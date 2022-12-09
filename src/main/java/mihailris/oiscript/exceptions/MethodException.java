package mihailris.oiscript.exceptions;

public class MethodException extends RuntimeException {
    public MethodException(String name) {
        super("object has no method '"+name+"'");
    }
}
