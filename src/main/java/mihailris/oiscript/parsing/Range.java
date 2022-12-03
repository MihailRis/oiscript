package mihailris.oiscript.parsing;

import java.util.Iterator;

public class Range implements Iterable<Long> {
    private final long start;
    private final long end;
    public Range(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "<"+start+" to "+end+">";
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    @Override
    public Iterator<Long> iterator() {
        return new RangeIterator(start, end);
    }

    public static class RangeIterator implements Iterator<Long> {
        private long i;
        private final long end;
        private boolean ended;

        public RangeIterator(long start, long end) {
            this.i = start;
            this.end = end;
        }

        @Override
        public boolean hasNext() {
            return !ended;
        }

        @Override
        public Long next() {
            if (i < end) {
                return i++;
            } else {
                if (i == end)
                    ended = true;
                return i--;
            }
        }
    }
}
