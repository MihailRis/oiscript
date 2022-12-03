package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.exceptions.BreakSignal;
import mihailris.oiscript.exceptions.ContinueSignal;

import java.util.Collection;
import java.util.List;

public class ForLoop extends Command {
    private final String name;
    private final Value iterable;
    private final List<Command> commands;
    public ForLoop(Position position, String name, Value iterable, List<Command> commands) {
        super(position);
        this.name = name;
        this.iterable = iterable;
        this.commands = commands;
    }

    @Override
    public void execute(Context context) {
        try {
            Object iterableValue = iterable.eval(context);
            if (iterableValue instanceof Number) {
                Number number = (Number) iterableValue;
                long count = number.longValue();
                for (int i = 0; i < count; i++) {
                    context.namespace.put(name, i);
                    for (Command command : commands) {
                        try {
                            command.execute(context);
                            if (context.returned)
                                return;
                        } catch (ContinueSignal ignored) {
                        }
                    }
                }
            } else if (iterableValue instanceof Iterable) {
                Iterable<?> collection = (Iterable<?>) iterableValue;
                for (Object object : collection) {
                    context.namespace.put(name, object);
                    for (Command command : commands) {
                        try {
                            command.execute(context);
                            if (context.returned)
                                return;
                        } catch (ContinueSignal ignored) {
                        }
                    }
                }
            }
        } catch (BreakSignal ignored){
        }
    }

    @Override
    public String toString() {
        return super.toString()+"<for "+name+" : "+iterable+">";
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }
}
