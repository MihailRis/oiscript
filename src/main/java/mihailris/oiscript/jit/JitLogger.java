package mihailris.oiscript.jit;

import org.objectweb.asm.Label;

public class JitLogger {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    void log(String text) {
        System.out.print(";  ");
        System.out.print(ANSI_GREEN);
        System.out.print(text);
        System.out.println(ANSI_RESET);
    }

    void log(String text, Object arg) {
        System.out.print(";  ");
        System.out.print(ANSI_GREEN);
        System.out.print(text);
        System.out.print(ANSI_RESET);
        System.out.println(arg);
    }

    void log(Label label) {
        System.out.print(WHITE_BOLD_BRIGHT);
        System.out.print("."+label+":");
        System.out.println(ANSI_RESET);
    }

    void comment(String text) {
        System.out.print(ANSI_BLUE);
        System.out.print("### ");
        System.out.print(text);
        System.out.println(ANSI_RESET);
    }
}
