package mihailris.oiscript;

import mihailris.oiscript.exceptions.MemberNotDefinedException;
import mihailris.oiscript.exceptions.NameException;
import mihailris.oiscript.exceptions.RuntimeInterruptedException;
import mihailris.oiscript.runtime.Function;
import mihailris.oiscript.runtime.OiRunHandle;
import mihailris.oiscript.runtime.Procedure;
import mihailris.oiscript.stdlib.LibMath;

import java.util.List;
import java.util.Map;

import static mihailris.oiscript.Keywords.INCLUDED;

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

        set(INCLUDED, new OiObject());
    }

    @Override
    public String toString() {
        return "<script "+filename+">";
    }

    public void prepare() {
        for (Map.Entry<String, Function> entry : functions.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
        for (String include : includes) {
            Object object = get(include);
            if (object == null)
                throw new NameException("unable to include '"+include+"' - not defined");
            OiObject included = (OiObject) object;
            getOi(INCLUDED).extend(included);
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
        Context context = new Context(this, runHandle);
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
        Context context = new Context(this, runHandle);
        Thread thread = new Thread(() -> {
            try {
                procedure.execute(context, args);
            } catch (Exception e) {
                runHandle.exception = e;
            }
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

    public OiObject getOi(String name) {
        Object object = get(name);
        if (object == null)
            return null;
        return (OiObject) object;
    }

    public void include(OiObject oiObject) {
        OiObject included = getOi(INCLUDED);
        included.extend(oiObject);
    }
}
