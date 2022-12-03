package mihailris.oiscript.parsing;

public class Ternary extends Value {
    private final Value condition;
    private final Value valueA;
    private final Value valueB;
    public Ternary(Value condition, Value valueA, Value valueB) {
        this.condition = condition;
        this.valueA = valueA;
        this.valueB = valueB;
    }

    @Override
    public String toString() {
        return condition+" ? "+valueA+" : "+valueB;
    }
}
