package mihailris.oiscript.runtime;

import mihailris.oiscript.Context;
import mihailris.oiscript.parsing.Command;
import mihailris.oiscript.parsing.ScriptComponent;
import mihailris.oiscript.parsing.Value;

import java.util.List;
import java.util.Vector;

public class Function extends Value implements ScriptComponent {
    private final String name;
    private final List<String> args;
    private final List<Command> commands;
    private String restArg;

    public Function(String name, List<String> args, List<Command> commands) {
        this.name = name;
        this.args = args;
        this.commands = commands;
        if (args.size() > 0 && args.get(args.size()-1).charAt(0) == '*') {
            restArg = args.get(args.size() - 1);
            args.remove(restArg);
            restArg = restArg.substring(1);
        }
    }

    @Override
    public String toString() {
        String argsStr = String.valueOf(args);
        String rest = restArg == null ? "" : "*"+restArg;
        if (!args.isEmpty() && !rest.isEmpty()) rest = ", "+rest;
        if (name == null)
            return "<function ("+argsStr.substring(1, argsStr.length()-1)+rest+")>";
        return "<function "+name+"("+argsStr.substring(1, argsStr.length()-1)+rest+")>";
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }

    public String getRestArg() {
        return restArg;
    }

    public List<Command> getCommands() {
        return commands;
    }

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
