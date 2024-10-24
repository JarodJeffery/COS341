package main;

import java.io.File;
import java.util.List;

import generator.IntermediateCodeGenerator;
import lexer.Lexer;
import parser.Parser;
import semantic.SemanticAnalyzer;
import semantic.TableEntry;

public class Main {

    @SuppressWarnings("UseSpecificCatch")
    public static void main(String[] args) {
        String inputDir = "txt"; // Directory for input .txt files
        String outputDir = "lexer_outputs"; // Directory for output XML files
        File folder = new File(inputDir);
        System.out.println("here");
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Error: 'txt' directory not found.");
            return;
        }

        // Ensure the output directory exists
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists()) {
            outputFolder.mkdir(); // Create the output directory if it doesn't exist
        }

        // List all .txt files in the txt/ directory
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (listOfFiles == null || listOfFiles.length == 0) {
            System.out.println("No .txt files found in the 'txt' directory.");
            return;
        }

        // Process each .txt file
        for (File inputFile : listOfFiles) {
            String inputFilePath = inputFile.getAbsolutePath(); // Get the full path of the .txt file
            String outputXmlPath = generateOutputFileName(outputDir, inputFile.getName()); // Dynamically generate output XML
            try {
                // Create the Lexer instance and process the input file
                @SuppressWarnings("unused")
                Lexer lexer = new Lexer(inputFilePath, outputXmlPath);
                System.out.println("Processed: " + inputFile.getName() + " -> " + outputXmlPath);

                // Pass the XML output to the parser
                Parser parser = new Parser();
                parser.readTokensFromXML(outputXmlPath); // Read tokens from XML file
                parser.parseTokens();

                SemanticAnalyzer sa = new SemanticAnalyzer(parser.getParseTree());
                sa.analyze();

                List<TableEntry> symbolTable = sa.getSymbolTable();
                System.out.println("Intermediate code generator");
                IntermediateCodeGenerator icg = new IntermediateCodeGenerator(symbolTable);
                icg.displayIntermediateCode();
                icg.writeIntermediateCodeToFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Method to generate a dynamic output XML file name based on input file name
    private static String generateOutputFileName(String outputDir, String inputFileName) {
        String baseName = inputFileName.replace(".txt", ""); // Remove .txt extension
        return outputDir + "/Lexer_out_" + baseName + ".xml"; // Save output in the output/ directory
    }

    private static String generateOutputFileNameCode(String outputDir, String inputFileName) {
        String baseName = inputFileName.replace(".xml", ""); // Remove .txt extension
        return outputDir + "/codeGen_out_" + baseName + ".bas"; // Save output in the output/ directory
    }
}
