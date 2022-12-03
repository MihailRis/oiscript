package mihailris.oiscript.exceptions;

public class ContinueSignal extends RuntimeException {
    public ContinueSignal() {
        super(null, null, true, false);
    }
}