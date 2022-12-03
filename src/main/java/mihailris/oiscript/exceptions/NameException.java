package mihailris.oiscript.exceptions;

public class NameException extends RuntimeException {
    public NameException(String name) {
        super("'"+name+"' is not defined in scope");
    }
}
