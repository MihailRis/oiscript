package mihailris.oiscript;

import mihailris.oiscript.exceptions.MemberNotDefinedException;
import mihailris.oiscript.exceptions.NameException;
import mihailris.oiscript.exceptions.RuntimeInterruptedException;
import mihailris.oiscript.runtime.Function;
import mihailris.oiscript.runtime.OiRunHandle;
import mihailris.oiscript.runtime.Procedure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Script extends OiObject {
    private final String filename;
    private final Map<String, Function> functions;
    private final Map<String, Procedure> procedures;
    private final List<String> includes;

    public Script(String filename, Map<String, Function> functions, Map<String, Procedure> procedures, List<String> includes) {
        this.filename = filename;
        this.functions = functions;
        this.procedures = procedures;
        this.includes = includes;
        set("script", this);
    }

    @Override
    public String toString() {
        return "<script "+filename+">";
    }

    public void prepare() {
        System.out.println(functions);
        for (Map.Entry<String, Function> entry : functions.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
        for (String include : includes) {
            Object object = get(include);
            if (object == null)
                throw new NameException("unable to include '"+include+"' - not defined");
            OiObject included = (OiObject) object;
            extend(included);
        }
    }

    public Object execute(String functionName, Object... args) {
        Function function = functions.get(functionName);
        if (function == null)
            throw new MemberNotDefinedException(this, functionName, "function '"+functionName+
                    "' is not defined in script '"+filename+"'");
        return execute(function, new OiRunHandle(), args);
    }

    public Object execute(Function function, OiRunHandle runHandle, Object... args) {
        Context context = new Context(this, runHandle, new HashMap<>());
        return function.execute(context, args);
    }

    public OiRunHandle start(String procedureName, Object... args) {
        Procedure procedure = procedures.get(procedureName);
        if (procedure == null)
            throw new MemberNotDefinedException(this, procedureName, "procedure '"+procedureName+
                    "' is not defined in script '"+filename+"'");
        return start(procedure, new OiRunHandle(), args);
    }

    public OiRunHandle start(Procedure procedure, OiRunHandle runHandle, Object... args) {
        Context context = new Context(this, runHandle, new HashMap<>());
        Thread thread = new Thread(() -> {
            procedure.execute(context, args);
            runHandle.finished = true;
            synchronized (runHandle.lock){
                runHandle.lock.notifyAll();
            }
        });
        thread.setDaemon(true);
        thread.setName("OiScript("+filename+")");
        runHandle.thread = thread;
        synchronized (runHandle.lock) {
            thread.start();
            try {
                runHandle.lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        }
        return runHandle;
    }
}
