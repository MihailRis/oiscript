package mihailris.oiscript.runtime;

import mihailris.oiscript.Context;
import mihailris.oiscript.parsing.Command;
import mihailris.oiscript.parsing.ScriptComponent;

import java.util.ArrayList;
import java.util.List;

public class Procedure implements ScriptComponent {
    private final String name;
    private final List<String> args;
    protected final List<String> locals;
    private final List<Command> commands;

    public Procedure(String name, List<String> args, List<Command> commands) {
        this.name = name;
        this.args = args;
        this.commands = commands;
        this.locals = new ArrayList<>(args);
    }

    @Override
    public String toString() {
        String argsStr = String.valueOf(args);
        return "<procedure "+name+"("+argsStr.substring(1, argsStr.length()-1)+")>";
    }

    public List<String> getLocals() {
        return locals;
    }

    public int getLocalIndex(String name) {
        return locals.indexOf(name);
    }

    public int defineLocal(String name) {
        int index;
        if ((index = locals.indexOf(name)) != -1) {
            return index;
        }
        locals.add(name);
        return locals.size()-1;
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

    public void execute(Context context, Object[] args) {
        if (args.length != this.args.size()) {
            throw new IllegalArgumentException("proc "+name+": "+this.args.size()+" arguments excepted, got "+args.length);
        }
        for (int i = 0; i < args.length; i++) {
            String argName = this.args.get(i);
            context.namespace.put(argName, args[i]);
        }
        for (Command command : commands) {
            command.execute(context);
            if (context.returned)
                return;
        }
    }
}
