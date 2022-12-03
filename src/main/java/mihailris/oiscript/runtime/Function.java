package mihailris.oiscript.runtime;

import mihailris.oiscript.Context;
import mihailris.oiscript.parsing.Command;
import mihailris.oiscript.parsing.ScriptComponent;
import mihailris.oiscript.parsing.Value;

import java.util.List;

public class Function extends Value implements ScriptComponent {
    private final String name;
    private final List<String> args;
    private final List<Command> commands;

    public Function(String name, List<String> args, List<Command> commands) {
        this.name = name;
        this.args = args;
        this.commands = commands;
    }

    @Override
    public String toString() {
        String argsStr = String.valueOf(args);
        if (name == null)
            return "<function ("+argsStr.substring(1, argsStr.length()-1)+")>";
        return "<function "+name+"("+argsStr.substring(1, argsStr.length()-1)+")>";
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public Object execute(Context context, Object... args) {
        if (args.length != this.args.size()) {
            throw new IllegalArgumentException("func "+name+": "+this.args.size()+" arguments excepted, got "+args.length);
        }
        for (int i = 0; i < args.length; i++) {
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
