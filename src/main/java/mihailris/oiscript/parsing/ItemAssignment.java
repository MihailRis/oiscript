package mihailris.oiscript.parsing;

public class ItemAssignment extends Command {
    private final Value source;
    private final Value key;
    private final String operator;
    private final Value value;

    public ItemAssignment(Position position, Value source, Value key, String operator, Value value) {
        super(position);
        this.source = source;
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString()+source+"["+key+"] "+operator+" "+value;
    }
}
