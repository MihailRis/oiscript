package mihailris.oiscript;

import mihailris.oiscript.runtime.OiRunHandle;

import java.util.HashMap;
import java.util.Map;

public class Context {
    public final Script script;
    public final OiRunHandle runHandle;
    public final Map<String, Object> namespace;
    public boolean returned;
    public Object returnValue;

    public Context(Script script, OiRunHandle runHandle) {
        this.script = script;
        this.runHandle = runHandle;
        this.namespace = new HashMap<>();
    }

    public Context(Script script, OiRunHandle runHandle, Map<String, Object> namespace) {
        this.script = script;
        this.runHandle = runHandle;
        this.namespace = namespace;
    }

    public Object get(String name) {
        Object value = namespace.get(name);
        if (value == null) {
            return script.get(name);
        }
        return value;
    }
}
