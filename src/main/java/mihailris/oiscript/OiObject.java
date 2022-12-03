package mihailris.oiscript;

import mihailris.oiscript.parsing.Value;

import java.util.HashMap;
import java.util.Map;

public class OiObject extends Value {
    private final Map<Object, Object> members;

    public OiObject() {
        this.members = new HashMap<>();
    }

    public void set(Object key, Object value) {
        members.put(key, value);
    }

    public void extend(OiObject object) {
        members.putAll(object.members);
    }

    @Override
    public String toString() {
        return "<object "+Integer.toHexString(super.hashCode())+">";
    }

    @Override
    public Object eval(Context context) {
        return this;
    }

    public Object get(Object key) {
        return members.get(key);
    }

    public OiVector keys() {
        OiVector vector = new OiVector();
        vector.addAll(members.keySet());
        return vector;
    }

    public Object has(Object key) {
        return members.containsKey(key);
    }
}
