package mihailris.oiscript.parsing;

public class RestHolder extends Value {
    private final Value value;

    public RestHolder(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "*"+value;
    }
}
