package mihailris.oiscript.exceptions;

public class BreakSignal extends RuntimeException {
    public BreakSignal() {
        super(null, null, true, false);
    }
}
