package mihailris.oiscript;

import java.util.ArrayList;

public class OiVector extends ArrayList<Object> {
    public OiVector() {
        super();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < size(); i++) {
            Object value = get(i);
            if (value instanceof String) {
                builder.append('"').append(OiUtils.cat((String) value)).append('"');
            } else {
                builder.append(value);
            }
            if (i+1 < size())
                builder.append(", ");
        }
        builder.append(']');
        return builder.toString();
    }
}
