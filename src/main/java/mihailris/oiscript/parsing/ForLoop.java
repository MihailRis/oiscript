package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.SemanticContext;
import mihailris.oiscript.exceptions.BreakSignal;
import mihailris.oiscript.exceptions.ContinueSignal;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.runtime.Function;

import java.util.List;

public class ForLoop extends Command {
    private final String name;
    private Value iterable;
    private final List<Command> commands;
    private int index = -1;

    public ForLoop(Position position, String name, Value iterable, List<Command> commands) {
        super(position);
        this.name = name;
        this.iterable = iterable;
        this.commands = commands;
    }

    @Override
    public Command build(SemanticContext context) throws ParsingException {
        iterable = iterable.build(context);
        Function function = context.getFunction();
        index = function.defineLocal(name);
        return super.build(context);
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
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
                    try {
                        for (Command command : commands) {
                            command.execute(context);
                            if (context.returned)
                                return;
                        }
                    } catch (ContinueSignal ignored) {
                    }
                }
            } else if (iterableValue instanceof String) {
                CharSequence sequence = (CharSequence) iterableValue;
                for (int i = 0; i < sequence.length(); i++) {
                    char c = sequence.charAt(i);
                    context.namespace.put(name, c);
                    try {
                        for (Command command : commands) {
                            command.execute(context);
                            if (context.returned)
                                return;
                        }
                    } catch (ContinueSignal ignored) {
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

    public Value getIterable() {
        return iterable;
    }
}
