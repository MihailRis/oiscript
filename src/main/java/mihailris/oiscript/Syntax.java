package mihailris.oiscript;

public class Syntax {
    public static boolean isBreakingChar(char chr) {
        return Character.isWhitespace(chr) || Operators.isOperatorChar(chr) || chr == '.' ||
                chr == '(' || chr == ')' ||
                chr == '[' || chr == ']' ||
                chr == ':' || chr == ',' || chr == ';' ||
                chr == '{' || chr == '}';
    }
}
