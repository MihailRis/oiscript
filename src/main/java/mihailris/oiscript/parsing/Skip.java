package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;

public class Skip extends Command {
    private final Value durationValue;

    public Skip(Position position, Value durationValue) {
        super(position);
        this.durationValue = durationValue;
    }

    @Override
    public void execute(Context context) {
        long frames = 0;
        if (durationValue != null) {
            frames = ((Number) durationValue.eval(context)).longValue();
        }
        context.runHandle.skip(frames);
    }

    @Override
    public String toString() {
        return super.toString()+"skip "+durationValue;
    }
}
