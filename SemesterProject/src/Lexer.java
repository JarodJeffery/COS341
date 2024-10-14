import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Lexer {

    private final String input;
    private int pos;
    private FileWriter xmlWriter;

    public Lexer(String input, String outputFileName) {
        this.input = input;
        this.pos = 0;
        try {
            this.xmlWriter = new FileWriter(outputFileName);
            writeXmlHeader();  // Initialize the XML file with a root element
        } catch (IOException e) {
            e.printStackTrace();
        }
        tokenizeInput(); // Tokenize the whole input when the lexer is initialized
    }

    // Tokenizes the whole input and writes all tokens to XML
    private void tokenizeInput() {
        Token token;
        do {
            token = nextToken();
            writeTokenToXml(token);  // Write each token to XML
        } while (token.type != Token.TokenType.EOF);

        closeXmlFile();  // Close XML after all tokens are written
    }

    // Method to get the next token
    public Token nextToken() {
        if (pos >= input.length()) {
            return new Token(Token.TokenType.EOF, "");
        }

        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++; // Skip whitespaces
        }

        for (Token.TokenType type : Token.TokenType.values()) {
            if (type == Token.TokenType.EOF) {
                continue;  // Skip EOF pattern check
            }
            Pattern pattern = Pattern.compile("^" + type.getPattern());
            Matcher matcher = pattern.matcher(input.substring(pos));

            if (matcher.find()) {
                String tokenValue = matcher.group();
                pos += tokenValue.length();
                return new Token(type, tokenValue);
            }
        }

        throw new RuntimeException("Unrecognized token at position " + pos);
    }

    // Helper method to write the XML header
    private void writeXmlHeader() throws IOException {
        xmlWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xmlWriter.write("<tokens>\n"); // Start of the XML document
    }

    // Helper method to write a token to the XML file
    private void writeTokenToXml(Token token) {
        try {
            xmlWriter.write("  <token>\n");
            xmlWriter.write("    <type>" + token.type + "</type>\n");
            xmlWriter.write("    <value>" + token.value + "</value>\n");
            xmlWriter.write("  </token>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to close the XML file
    private void closeXmlFile() {
        try {
            xmlWriter.write("</tokens>\n"); // Close the root element
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
