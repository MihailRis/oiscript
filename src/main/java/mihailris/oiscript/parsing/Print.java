package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class Print extends Command {
    private final Value value;
    public Print(Position position, Value value) {
        super(position);
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public void execute(Context context) {
        Object object = value.eval(context);
        if (object instanceof Number) {
            Number number = (Number) object;
            double dvalue = number.doubleValue();
            long lvalue = number.longValue();
            if (dvalue == lvalue) {
                System.out.print(lvalue);
                return;
            }
        }
        System.out.print(object);
    }

    @Override
    public String toString() {
        return super.toString()+"print "+value;
    }
}
