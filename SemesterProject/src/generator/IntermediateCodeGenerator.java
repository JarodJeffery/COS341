package generator;

import semantic.TableEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IntermediateCodeGenerator {
    private List<TableEntry> symbolTable;

    public IntermediateCodeGenerator(List<TableEntry> symbolTable) {
        this.symbolTable = symbolTable;
    }

    // Generate the intermediate code
    public List<String> generateIntermediateCode() {
        List<String> intermediateCode = new ArrayList<>();

        for (TableEntry entry : symbolTable) {
            String codeLine = generateCodeForEntry(entry);
            intermediateCode.add(codeLine);
        }

        return intermediateCode;
    }

    // Generate code for a single TableEntry
    private String generateCodeForEntry(TableEntry entry) {
        StringBuilder code = new StringBuilder();

        if (entry.isFunction()) {
            // Handle function entries
            code.append("FUNCTION ").append(entry.getOriginalName())
                    .append(" (").append(entry.getInternalName()).append(")")
                    .append(" // Scope: ").append(entry.getScopeId())
                    .append(", Parent Scope: ").append(entry.getParentScopeId());
        } else {
            // Handle variable entries
            code.append("VAR ").append(entry.getOriginalName())
                    .append(" : ").append(entry.getType())
                    .append(" // Internal: ").append(entry.getInternalName())
                    .append(", Scope: ").append(entry.getScopeId())
                    .append(", Parent Scope: ").append(entry.getParentScopeId());
        }

        return code.toString();
    }

    // Write intermediate code to a file
    public void writeIntermediateCodeToFile() {
        List<String> intermediateCode = generateIntermediateCode();
        File folder = new File("intermediate");
        if (!folder.exists()) {
            folder.mkdir(); // Create the folder if it doesn't exist
        }

        File file = new File(folder, "intermediate.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : intermediateCode) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Intermediate code successfully written to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing intermediate code to file: " + e.getMessage());
        }
    }

    // Optional: Display the generated intermediate code in the console
    public void displayIntermediateCode() {
        List<String> intermediateCode = generateIntermediateCode();
        for (String line : intermediateCode) {
            System.out.println(line);
        }
    }
}
