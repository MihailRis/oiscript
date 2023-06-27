package mihailris.oiscript.runtime;

import mihailris.oiscript.Context;
import mihailris.oiscript.parsing.ScriptComponent;
import mihailris.oiscript.parsing.Value;

import java.util.ArrayList;
import java.util.List;

public abstract class Function extends Value implements ScriptComponent, OiExecutable {
    protected final String name;
    protected final List<String> args;
    protected final List<String> locals;
    protected String restArg;
    private boolean mainThreadOnly;

    protected Function(String name, List<String> args) {
        this.name = name;
        this.args = args;
        if (args.size() > 0 && args.get(args.size()-1).charAt(0) == '*') {
            restArg = args.get(args.size() - 1);
            args.remove(restArg);
            restArg = restArg.substring(1);
        }
        this.locals = new ArrayList<>(args);
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

    @Override
    public Object eval(Context context) {
        return this;
    }

    public boolean isMainThreadOnly() {
        return mainThreadOnly;
    }

    public void setMainThreadOnly(boolean mainThreadOnly) {
        this.mainThreadOnly = mainThreadOnly;
    }

    public List<String> getLocals() {
        return locals;
    }
}
