package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class StringValue extends Value {
    private final String string;

    public StringValue(String string) {
        this.string = string;
    }

    @Override
    public Object eval(Context context) {
        return string;
    }

    @Override
    public String toString() {
        return '"'+string+'"';
    }
}
