package mihailris.oiscript;

import mihailris.oiscript.parsing.Value;

public class OiNone extends Value {
    public static final OiNone NONE = new OiNone();
    private OiNone(){
    }

    @Override
    public Object eval(Context context) {
        return this;
    }

    @Override
    public String toString() {
        return "none";
    }
}
