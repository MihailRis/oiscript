package mihailris.oiscript.exceptions;

public class AttributeException extends RuntimeException{
    public AttributeException(String name) {
        super("object has no attribute '"+name+"'");
    }
}
