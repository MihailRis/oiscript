package mihailris.oiscript;

import java.util.HashMap;
import java.util.Map;

public class OiObject {
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

    public Object get(Object key) {
        return members.get(key);
    }

    public OiVector keys() {
        OiVector vector = new OiVector();
        vector.addAll(members.keySet());
        return vector;
    }
}
