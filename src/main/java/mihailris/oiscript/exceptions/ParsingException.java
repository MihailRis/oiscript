package mihailris.oiscript.exceptions;

import mihailris.oiscript.parsing.Position;
import mihailris.oiscript.Source;

import java.io.PrintStream;

public class ParsingException extends Exception {
    private final Source source;
    private final Position position;
    public ParsingException(Source source, Position position, String message) {
        super(message);
        this.source = source;
        this.position = position;
    }

    public void printOiErrorTrace() {
        printOiErrorTrace(System.err);
    }

    public void printOiErrorTrace(PrintStream err) {
        String fragment = source.getSource().substring(position.getLinePos());
        if (fragment.indexOf('\n') >= 0)
            fragment = fragment.substring(0, fragment.indexOf('\n'));
        err.println(fragment);
        for (int i = 0; i < position.getLocalPos(); i++) {
            System.err.print(' ');
        }
        err.println('^');
        err.println("error at "+(position.getLine()+1)+":"+position.getLocalPos()+" -> "+getMessage());
    }

    public Source getSource() {
        return source;
    }

    public Position getPosition() {
        return position;
    }
}
