package mihailris.oiscript;

import mihailris.oiscript.parsing.Command;
import mihailris.oiscript.runtime.Function;

import java.util.List;
import java.util.Vector;

public class RawFunction extends Function {
    private final List<Command> commands;

    public RawFunction(String name, List<String> args, List<Command> commands) {
        super(name, args);
        this.commands = commands;
    }

    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public Object execute(Context context, Object... args) {
        if (restArg != null){
            Vector<Object> rest = new Vector<>();
            for (int i = this.args.size(); i < args.length; i++) {
                rest.add(args[i]);
            }
            context.namespace.put(restArg, rest);
        }
        if (args.length != this.args.size()) {
            if (args.length < this.args.size() || restArg == null){
                throw new IllegalArgumentException("func " + name + ": " + this.args.size() + " arguments excepted, got " + args.length);
            }
        }
        for (int i = 0; i < this.args.size(); i++) {
            String argName = this.args.get(i);
            context.namespace.put(argName, args[i]);
        }
        for (Command command : commands) {
            command.execute(context);
            if (context.returned)
                return context.returnValue;
        }
        return 0;
    }
}
