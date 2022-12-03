package mihailris.oiscript.exceptions;

public class RuntimeInterruptedException extends RuntimeException {
    public RuntimeInterruptedException(InterruptedException exception) {
        super(exception);
    }
}
