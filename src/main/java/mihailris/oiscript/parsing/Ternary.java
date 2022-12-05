package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.Logics;

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
    public Object eval(Context context) {
        return Logics.isTrue(condition.eval(context)) ? valueA.eval(context) : valueB.eval(context);
    }

    @Override
    public String toString() {
        return condition+" ? "+valueA+" : "+valueB;
    }
}
