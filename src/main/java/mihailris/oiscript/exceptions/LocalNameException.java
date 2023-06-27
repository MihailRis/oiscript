package mihailris.oiscript.exceptions;

public class LocalNameException extends RuntimeException {
    public LocalNameException(String name) {
        super("'"+name+"' referred before assignment");
    }
}
