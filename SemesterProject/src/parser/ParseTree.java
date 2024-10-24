package parser;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.*;

public class ParseTree {
    private RootNode root;
    private List<InnerNode> innerNodes;
    private List<LeafNode> leafNodes;
    private int indentLevel = 0;

    public ParseTree() {
        this.innerNodes = new ArrayList<>();
        this.leafNodes = new ArrayList<>();
    }

    public void setRoot(RootNode root) {
        this.root = root;
    }

    public RootNode getRoot() {
        return root;
    }

    public void addInnerNode(InnerNode node) {
        innerNodes.add(node);
    }

    public List<InnerNode> getInnerNodes() {
        return innerNodes;
    }

    public List<LeafNode> getLeafNodes() {
        return leafNodes;
    }

    public void addLeafNode(LeafNode node) {
        leafNodes.add(node);
    }

    private void indent(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters("\n" + "  ".repeat(indentLevel));
    }

    public void writeToXML(String filePath) throws XMLStreamException, java.io.IOException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(new FileWriter(filePath));

        writer.writeStartElement("SYNTREE");
        indentLevel++;

        // Write ROOT
        indent(writer);
        writer.writeStartElement("ROOT");
        indentLevel++;

        indent(writer);
        writer.writeStartElement("UNID");
        writer.writeCharacters(String.valueOf(root.getUnid()));
        writer.writeEndElement();

        indent(writer);
        writer.writeStartElement("SYMB");
        writer.writeCharacters(root.getStartSymbol());
        writer.writeEndElement();

        indent(writer);
        writer.writeStartElement("CHILDREN");
        indentLevel++;
        for (Integer childId : root.getChildrenIds()) {
            indent(writer);
            writer.writeStartElement("ID");
            writer.writeCharacters(String.valueOf(childId));
            writer.writeEndElement();
        }
        indentLevel--;
        indent(writer);
        writer.writeEndElement(); // End CHILDREN

        indentLevel--;
        indent(writer);
        writer.writeEndElement(); // End ROOT

        // Write INNERNODES
        indent(writer);
        writer.writeStartElement("INNERNODES");
        indentLevel++;

        for (InnerNode node : innerNodes) {
            indent(writer);
            writer.writeStartElement("IN");
            indentLevel++;

            indent(writer);
            writer.writeStartElement("PARENT");
            writer.writeCharacters(String.valueOf(node.getParentId()));
            writer.writeEndElement();

            indent(writer);
            writer.writeStartElement("UNID");
            writer.writeCharacters(String.valueOf(node.getUnid()));
            writer.writeEndElement();

            indent(writer);
            writer.writeStartElement("SYMB");
            writer.writeCharacters(node.getNonterminal());
            writer.writeEndElement();

            indent(writer);
            writer.writeStartElement("CHILDREN");
            indentLevel++;
            for (Integer childId : node.getChildrenIds()) {
                indent(writer);
                writer.writeStartElement("ID");
                writer.writeCharacters(String.valueOf(childId));
                writer.writeEndElement();
            }
            indentLevel--;
            indent(writer);
            writer.writeEndElement(); // End CHILDREN

            indentLevel--;
            indent(writer);
            writer.writeEndElement(); // End IN
        }

        indentLevel--;
        indent(writer);
        writer.writeEndElement(); // End INNERNODES

        // Write LEAFNODES
        indent(writer);
        writer.writeStartElement("LEAFNODES");
        indentLevel++;

        for (LeafNode node : leafNodes) {
            indent(writer);
            writer.writeStartElement("LEAF");
            indentLevel++;

            indent(writer);
            writer.writeStartElement("PARENT");
            writer.writeCharacters(String.valueOf(node.getParentId()));
            writer.writeEndElement();

            indent(writer);
            writer.writeStartElement("UNID");
            writer.writeCharacters(String.valueOf(node.getUnid()));
            writer.writeEndElement();

            indent(writer);
            writer.writeStartElement("TERMINAL");
            writer.writeCharacters(node.getToken().getValue());
            writer.writeEndElement();

            indentLevel--;
            indent(writer);
            writer.writeEndElement(); // End LEAF
        }

        indentLevel--;
        indent(writer);
        writer.writeEndElement(); // End LEAFNODES

        indentLevel--;
        indent(writer);
        writer.writeEndElement(); // End SYNTREE
        writer.writeEndDocument();
        writer.close();
    }
}