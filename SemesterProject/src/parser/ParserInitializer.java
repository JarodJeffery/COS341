package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParserInitializer {
    public static List<GrammarRule> initializeGrammarRules() {
        List<GrammarRule> grammarRules = new ArrayList<>();

        // Add the grammar rules
        grammarRules.add(new GrammarRule("PROG'", Arrays.asList("PROG"))); // Augmented rule
        grammarRules.add(new GrammarRule("PROG", Arrays.asList("main", "GLOBVARS", "ALGO", "FUNCTIONS")));
        grammarRules.add(new GrammarRule("GLOBVARS", Collections.emptyList())); // GLOBVARS -> ''
        grammarRules.add(new GrammarRule("GLOBVARS", Arrays.asList("VTYP", "VNAME", ",", "GLOBVARS")));
        grammarRules.add(new GrammarRule("VTYP", Arrays.asList("N")));
        grammarRules.add(new GrammarRule("VTYP", Arrays.asList("T")));
        grammarRules.add(new GrammarRule("VNAME", Arrays.asList("V")));
        grammarRules.add(new GrammarRule("ALGO", Arrays.asList("begin", "INSTRUC", "end")));
        grammarRules.add(new GrammarRule("INSTRUC", Collections.emptyList())); // INSTRUC -> ''
        grammarRules.add(new GrammarRule("INSTRUC", Arrays.asList("COMMAND", ";", "INSTRUC")));
        grammarRules.add(new GrammarRule("COMMAND", Arrays.asList("skip")));
        grammarRules.add(new GrammarRule("COMMAND", Arrays.asList("halt")));
        grammarRules.add(new GrammarRule("COMMAND", Arrays.asList("print", "ATOMIC")));
        grammarRules.add(new GrammarRule("COMMAND", Arrays.asList("return", "ATOMIC")));
        grammarRules.add(new GrammarRule("COMMAND", Arrays.asList("ASSIGN")));
        grammarRules.add(new GrammarRule("COMMAND", Arrays.asList("CALL")));
        grammarRules.add(new GrammarRule("COMMAND", Arrays.asList("BRANCH")));
        grammarRules.add(new GrammarRule("ATOMIC", Arrays.asList("VNAME")));
        grammarRules.add(new GrammarRule("ATOMIC", Arrays.asList("CONST")));
        grammarRules.add(new GrammarRule("CONST", Arrays.asList("N")));
        grammarRules.add(new GrammarRule("CONST", Arrays.asList("T")));
        grammarRules.add(new GrammarRule("ASSIGN", Arrays.asList("VNAME", "<", "input")));
        grammarRules.add(new GrammarRule("ASSIGN", Arrays.asList("VNAME", "=", "TERM")));
        grammarRules.add(new GrammarRule("CALL", Arrays.asList("FNAME", "(", "ATOMIC", ",", "ATOMIC", ",", "ATOMIC", ")")));
        grammarRules.add(new GrammarRule("BRANCH", Arrays.asList("if", "COND", "then", "ALGO", "else", "ALGO")));
        grammarRules.add(new GrammarRule("TERM", Arrays.asList("ATOMIC")));
        grammarRules.add(new GrammarRule("TERM", Arrays.asList("CALL")));
        grammarRules.add(new GrammarRule("TERM", Arrays.asList("OP")));
        grammarRules.add(new GrammarRule("OP", Arrays.asList("UNOP", "(", "ARG", ")")));
        grammarRules.add(new GrammarRule("OP", Arrays.asList("BINOP", "(", "ARG", ",", "ARG", ")")));
        grammarRules.add(new GrammarRule("ARG", Arrays.asList("ATOMIC")));
        grammarRules.add(new GrammarRule("ARG", Arrays.asList("OP")));
        grammarRules.add(new GrammarRule("COND", Arrays.asList("SIMPLE")));
        grammarRules.add(new GrammarRule("COND", Arrays.asList("COMPOSIT")));
        grammarRules.add(new GrammarRule("SIMPLE", Arrays.asList("BINOP", "(", "ATOMIC", ",", "ATOMIC", ")")));
        grammarRules.add(new GrammarRule("COMPOSIT", Arrays.asList("BINOP", "(", "SIMPLE", ",", "SIMPLE", ")")));
        grammarRules.add(new GrammarRule("COMPOSIT", Arrays.asList("UNOP", "(", "SIMPLE", ")")));
        grammarRules.add(new GrammarRule("UNOP", Arrays.asList("not")));
        grammarRules.add(new GrammarRule("UNOP", Arrays.asList("sqrt")));
        grammarRules.add(new GrammarRule("BINOP", Arrays.asList("or")));
        grammarRules.add(new GrammarRule("BINOP", Arrays.asList("and")));
        grammarRules.add(new GrammarRule("BINOP", Arrays.asList("eq")));
        grammarRules.add(new GrammarRule("BINOP", Arrays.asList("grt")));
        grammarRules.add(new GrammarRule("BINOP", Arrays.asList("add")));
        grammarRules.add(new GrammarRule("BINOP", Arrays.asList("sub")));
        grammarRules.add(new GrammarRule("BINOP", Arrays.asList("mul")));
        grammarRules.add(new GrammarRule("BINOP", Arrays.asList("div")));
        grammarRules.add(new GrammarRule("FNAME", Arrays.asList("F")));
        grammarRules.add(new GrammarRule("FUNCTIONS", Collections.emptyList())); // FUNCTIONS -> ''
        grammarRules.add(new GrammarRule("FUNCTIONS", Arrays.asList("DECL", "FUNCTIONS")));
        grammarRules.add(new GrammarRule("DECL", Arrays.asList("HEADER", "BODY")));
        grammarRules
                .add(new GrammarRule("HEADER", Arrays.asList("FTYP", "FNAME", "(", "VNAME", ",", "VNAME", ",", "VNAME", ")")));
        grammarRules.add(new GrammarRule("FTYP", Arrays.asList("num")));
        grammarRules.add(new GrammarRule("FTYP", Arrays.asList("void")));
        grammarRules.add(new GrammarRule("BODY", Arrays.asList("PROLOG", "LOCVARS", "ALGO", "EPILOG", "SUBFUNCS", "end")));
        grammarRules.add(new GrammarRule("PROLOG", Arrays.asList("{")));
        grammarRules.add(new GrammarRule("EPILOG", Arrays.asList("}")));
        grammarRules.add(
                new GrammarRule("LOCVARS", Arrays.asList("VTYP", "VNAME", ",", "VTYP", "VNAME", ",", "VTYP", "VNAME", ",")));
        grammarRules.add(new GrammarRule("SUBFUNCS", Arrays.asList("FUNCTIONS")));

        return grammarRules;
    }

    public static ParsingTable initializeParsingTable() {
        ParsingTable parsingTable = new ParsingTable();

        String csvFile = "Actions_GOTOS.csv";
        String line;
        String splitBy = ";";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                // Handling the case where a symbol might be quoted (like ";")
                String[] data = parseCsvLine(line, splitBy);

                int state = Integer.parseInt(data[0].trim());
                String symbol = data[1].trim();
                String type = data[2].trim();
                String value = data[3].trim();

                if (type.equals("ACTION")) {
                    parsingTable.addAction(state, symbol, value);
                } else if (type.equals("GOTO")) {
                    parsingTable.addGoto(state, symbol, Integer.parseInt(value));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parsingTable;
    }

    // Custom method to handle quoted fields in CSV
    private static String[] parseCsvLine(String line, String delimiter) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes; // Toggle the inQuotes flag
            } else if (c == delimiter.charAt(0) && !inQuotes) {
                // If not in quotes, split on the delimiter
                tokens.add(currentToken.toString().trim());
                currentToken.setLength(0);
            } else {
                // Otherwise, append the character to the current token
                currentToken.append(c);
            }
        }
        // Add the last token
        tokens.add(currentToken.toString().trim());

        return tokens.toArray(new String[0]);
    }

}
