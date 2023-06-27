package mihailris.oiscript.jit;

import java.util.Iterator;

public class OiIntegerIterable implements Iterable<Long> {
    private final long number;

    public OiIntegerIterable(long number) {
        this.number = number;
    }

    @Override
    public Iterator<Long> iterator() {
        return new OiIntegerIterator(number);
    }
}
