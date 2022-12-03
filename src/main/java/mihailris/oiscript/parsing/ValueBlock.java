package mihailris.oiscript.parsing;

public class ValueBlock extends Value {
    private final Value value;

    public ValueBlock(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "("+value+")";
    }
}
