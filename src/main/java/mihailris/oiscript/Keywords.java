package mihailris.oiscript;

public class Keywords {
    public static final String PROC = "proc";
    public static final String FUNC = "func";
    public static final String FOR = "for";
    public static final String PASS = "pass";
    public static final String IF = "if";
    public static final String ELIF = "elif";
    public static final String ELSE = "else";
    public static final String WHILE = "while";
    public static final String PRINT = "_print";
    public static final String WAIT = "wait";
    public static final String SKIP = "skip";
    public static final String RETURN = "return";
    public static final String TRY = "try";
    public static final String EXCEPT = "except";
    public static final String INCLUDE = "include";
    public static final String BREAK = "break";
    public static final String CONTINUE = "continue";
    public static final String SCRIPT = "script";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String NONE = "none";
    public static final String INIT = "_init";
    public static final String INCLUDED = "_included";

    public static boolean isReservedName(String name) {
        switch (name) {
            case TRUE:
            case FALSE:
            case NONE:
            case SCRIPT:
            case FOR:
            case TRY:
            case WHILE:
            case EXCEPT:
                return true;
            default:
                return false;
        }
    }
}
