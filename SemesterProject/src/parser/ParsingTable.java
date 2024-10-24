package parser;

import java.util.HashMap;
import java.util.Map;

public class ParsingTable {
    private Map<Integer, Map<String, String>> actionTable = new HashMap<>();
    private Map<Integer, Map<String, Integer>> gotoTable = new HashMap<>();

    // Method to add an action (shift, reduce, or accept)
    public void addAction(int state, String symbol, String action) {
        actionTable.computeIfAbsent(state, k -> new HashMap<>()).put(symbol, action);
    }

    // Method to add a goto entry (for non-terminals)
    public void addGoto(int state, String nonTerminal, int nextState) {
        gotoTable.computeIfAbsent(state, k -> new HashMap<>()).put(nonTerminal, nextState);
    }

    // Getter for the action table
    public String getAction(int state, String symbol) {
        return actionTable.getOrDefault(state, new HashMap<>()).get(symbol);
    }

    // Getter for the goto table
    public Integer getGoto(int state, String nonTerminal) {
        return gotoTable.getOrDefault(state, new HashMap<>()).get(nonTerminal);
    }

    public Map<Integer, Map<String, String>> getActionTable() {
        return actionTable;
    }

    public Map<Integer, Map<String, Integer>> getGotoTable() {
        return gotoTable;
    }

    public void printParsingTable() {
        System.out.println("Action Table:");
        for (Map.Entry<Integer, Map<String, String>> entry : actionTable.entrySet()) {
            int state = entry.getKey();
            Map<String, String> actions = entry.getValue();
            for (Map.Entry<String, String> action : actions.entrySet()) {
                System.out.println("State: " + state + ", Symbol: " + action.getKey() + ", Action: " + action.getValue());
            }
        }

        System.out.println("\nGoto Table:");
        for (Map.Entry<Integer, Map<String, Integer>> entry : gotoTable.entrySet()) {
            int state = entry.getKey();
            Map<String, Integer> gotos = entry.getValue();
            for (Map.Entry<String, Integer> goTo : gotos.entrySet()) {
                System.out.println("State: " + state + ", NonTerminal: " + goTo.getKey() + ", Goto: " + goTo.getValue());
            }
        }
    }

}
