import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class TokenReader {
    private List<Token> tokens = new ArrayList<>();

    public TokenReader(String xmlFilePath) {
        try {
            File inputFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList tokenList = doc.getElementsByTagName("TOK");
            for (int i = 0; i < tokenList.getLength(); i++) {
                Node node = tokenList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getElementsByTagName("ID").item(0).getTextContent();
                    String tokenClass = element.getElementsByTagName("CLASS").item(0).getTextContent();
                    String word = element.getElementsByTagName("WORD").item(0).getTextContent();
                    Token.TokenType type;
                    if (tokenClass.equals("reserved_keyword")){
                        type = Token.TokenType.INPUT;
                    }else {
                        type = Token.TokenType.valueOf(tokenClass.toUpperCase());
                    }

                    tokens.add(new Token(type, word));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }
}
