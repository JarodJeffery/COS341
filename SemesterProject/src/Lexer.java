import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Lexer {
  private final String input;
  private int pos;
  private FileWriter xmlWriter;
  private List<Token> tokens;
  private int tokenId; // Token ID counter

  public Lexer(String inputFilePath, String outputXmlPath) {
    this.input = readFile(inputFilePath);
    this.pos = 0;
    this.tokens = new ArrayList<>();
    this.tokenId = 1; // Initialize token ID counter
    try {
      this.xmlWriter = new FileWriter(outputXmlPath);
      writeXmlHeader();
    } catch (IOException e) {
      e.printStackTrace();
    }
    tokenizeInput(); // Tokenize input from the file
  }

  private String readFile(String fileName) {
    StringBuilder content = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = br.readLine()) != null) {
        content.append(line).append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    ;
    return content.toString().trim();
  }

  private void tokenizeInput() {
    Token token;
    do {
      token = nextToken();
      tokens.add(token);
      writeTokenToXml(token); // Write token to the XML file
    } while (token.getType() != Token.TokenType.EOF);
    closeXmlFile();
  }

  public Token nextToken() {
    if (pos >= input.length()) {
      return new Token(Token.TokenType.EOF, ""); // EOF token
    }

    // Skip over whitespace characters
    while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
      pos++;
    }

    // Iterate over all defined token types and match using regex
    for (Token.TokenType type : Token.TokenType.values()) {
      if (type == Token.TokenType.EOF) {
        continue; // Skip EOF token
      }

      Pattern pattern = Pattern.compile("^" + type.getPattern());
      Matcher matcher = pattern.matcher(input.substring(pos));

      if (matcher.find()) {
        String tokenValue = matcher.group();
        pos += tokenValue.length(); // move position to end of token
        return new Token(type, tokenValue); // Return matched token
      }
    }

    System.err.println("Unrecognized token at position: " + pos);
    System.err.println("Remaining input: " + input.substring(pos));
    throw new RuntimeException("Unrecognized token at position: " + pos);
  }

  // Helper method to write the XML header
  private void writeXmlHeader() {
    try {
      xmlWriter.write("<TOKENSTREAM>\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Helper method to write a single token to the XML file
  private void writeTokenToXml(Token token) {
    try {
      String tokenClass = mapTokenClass(token.getType());
      xmlWriter.write("\t<TOK>\n");
      xmlWriter.write("\t\t<ID>" + tokenId++ + "</ID>\n"); // Increment token ID
      xmlWriter.write("\t\t<CLASS>" + tokenClass + "</CLASS>\n");
      xmlWriter.write("\t\t<WORD>" + token.getValue() + "</WORD>\n");
      xmlWriter.write("\t</TOK>\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Map token types to their respective classes for XML output
  private String mapTokenClass(Token.TokenType type) {
    switch (type) {
      case MAIN:
        return "MAIN";
      case BEGIN:
        return "BEGIN";
      case END:
        return "END";
      case IF:
        return "IF";
      case THEN:
        return "THEN";
      case ELSE:
        return "ELSE";
      case PRINT:
        return "PRINT";
      case NUM:
        return "NUM";
      case TEXT:
        return "TEXT";
      case VOID:
        return "VOID";
      case SKIP:
        return "SKIP";
      case HALT:
        return "HALT";
      case RETURN:
        return "RETURN";
      case INPUT:
        return "reserved_keyword";
      case VNAME:
        return "VNAME"; // User-defined variable names
      case FNAME:
        return "FNAME"; // User-defined function names
      case STRING:
        return "STRING"; // Strings
      case NUMBER:
        return "NUMBER"; // Numbers
      case ASSIGNMENT:
        return "ASSIGNMENT";
      case INPUT_OPERATOR:
        return "input_operator"; // Recognize '<' for input
      case BINOP:
        return "BINOP";
      case UNOP:
        return "UNOP";
      case LPAREN:
        return  "LPAREN";
      case RPAREN:
        return  "RPAREN";
      case COMMA:
        return "comma";
      case SEMICOLON:
        return "semicolon";
      case LBRACE:
        return "lbrace";
      case RBRACE:
        return "rbrace";
      case EOF:
        return "EOF";
      default:
        throw new IllegalArgumentException("Unknown token type: " + type);
    }
  }

  // Helper method to close XML file
  private void closeXmlFile() {
    try {
      xmlWriter.write("</TOKENSTREAM>\n");
      xmlWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}