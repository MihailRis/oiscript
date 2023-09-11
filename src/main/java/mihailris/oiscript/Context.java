package mihailris.oiscript;

import mihailris.oiscript.exceptions.NameException;
import mihailris.oiscript.runtime.OiRunHandle;

import java.util.HashMap;
import java.util.Map;

import static mihailris.oiscript.Keywords.INCLUDED;

public class Context {
    public final Script script;
    public final OiRunHandle runHandle;
    public final Map<String, Object> namespace;
    public final Object[] locals;
    public boolean returned;
    public Object returnValue;
    public OiObject included;

    public Context(Script script, OiRunHandle runHandle, int locals) {
        this.script = script;
        this.runHandle = runHandle;
        this.namespace = new HashMap<>();
        this.locals = new Object[locals];

        included = script.getOi(INCLUDED);
    }

    public Object get(int index) {
        return locals[index];
    }

    public Object get(String name) {
        Object value = namespace.get(name);
        if (value == null) {
            value = script.get(name);
            if (value == null)
                return included.get(name);
            return value;
        }
        return value;
    }

    public Object require(String name) {
        Object value = get(name);
        if (value == null)
            throw new NameException(name);
        return value;
    }
}
