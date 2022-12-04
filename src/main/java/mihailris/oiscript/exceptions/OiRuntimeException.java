package mihailris.oiscript.exceptions;

import mihailris.oiscript.parsing.Position;

import java.io.PrintStream;

public class OiRuntimeException extends RuntimeException {
    private final Position position;

    public OiRuntimeException(Position position, Throwable throwable) {
        super(throwable);
        this.position = position;
    }

    public void printOiErrorTrace() {
        printOiErrorTrace(System.err);
    }

    public void printOiErrorTrace(PrintStream err) {
        err.println("error at line "+(position.getLine()+1)+" -> "+getMessage());
    }

    public Position getPosition() {
        return position;
    }
}
