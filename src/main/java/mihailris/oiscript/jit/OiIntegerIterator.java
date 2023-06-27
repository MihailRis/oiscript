package mihailris.oiscript.jit;

import java.util.Iterator;

public class OiIntegerIterator implements Iterator<Long> {
    final long number;
    long current;

    public OiIntegerIterator(long number) {
        this.number = number;
    }

    @Override
    public boolean hasNext() {
        return current < number;
    }

    @Override
    public Long next() {
        return current++;
    }
}
