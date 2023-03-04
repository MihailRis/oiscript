package mihailris.oiscript;

public class Operators {
    public static final String OPERATOR_CHARS = "+-*/!<>=%";

    public static boolean isOperatorChar(char c) {
        return OPERATOR_CHARS.indexOf(c) >= 0;
    }

    public static boolean isBreakingOperatorChar(char prev, char c) {
        return (c == '-' && prev != c) || (c == '+' && prev != c);
    }

    public static boolean isOperator(String text) {
        switch (text) {
            case ">": case ">=": case "<=": case "==": case "<":
            case "+": case "-": case "?":
            case ">>": case "<<": case ">>>":
            case "*": case "/": case "%": case "//": case "**":
            case "to": case "in": case "and": case "or": case "new":
                return true;
            default:
                return false;
        }
    }

    public static int operatorPriorety(String text) {
        switch (text) {
            case "?": return 3;
            case "or": return 4;
            case "and": return 5;
            case ">": case ">=": case "==": case "<=": case "<": case "in": return 6;
            case "to": return 7;
            case "<<": case ">>": case ">>>": return 8;
            case "+": case "-": return 9;
            case "*": case "/": case "%": case "//": return 10;
            case "**": return 11;
        }
        return -1;
    }

    public static boolean isUnaryOperator(String text) {
        switch (text) {
            case "-":
            case "+":
            case "*":
            case "!":
            case "new":
            case "not":
                return true;
        }
        return false;
    }
}
