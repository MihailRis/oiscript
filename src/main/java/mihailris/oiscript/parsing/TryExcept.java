package mihailris.oiscript.parsing;

import mihailris.oiscript.Context;
import mihailris.oiscript.exceptions.BreakSignal;
import mihailris.oiscript.exceptions.ContinueSignal;

import java.util.List;

public class TryExcept extends Command {
    private final List<Command> commands;
    private final String exceptionVariable;
    private final List<Command> exceptionCommands;

    public TryExcept(Position position, List<Command> commands, String exceptionVariable, List<Command> exceptionCommands) {
        super(position);
        this.commands = commands;
        this.exceptionVariable = exceptionVariable;
        this.exceptionCommands = exceptionCommands;
    }

    @Override
    public void execute(Context context) {
        try {
            for (Command command : commands) {
                command.execute(context);
                if (context.returned)
                    return;
            }
        } catch (BreakSignal | ContinueSignal e) {
            throw e;
        } catch (Exception e) {
            context.namespace.put(exceptionVariable, e);
            for (Command command : exceptionCommands) {
                command.execute(context);
                if (context.returned)
                    return;
            }
        }
    }

    @Override
    public String toString() {
        return super.toString()+"try-catch "+exceptionVariable;
    }

}
