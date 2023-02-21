package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.parsing.Parser;
import mihailris.oiscript.parsing.Value;
import mihailris.oiscript.runtime.OiModule;
import mihailris.oiscript.stdlib.LibMath;
import mihailris.oiscript.stdlib.LibStd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OI {
    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 9;
    public static final int VERSION_PATCH = 5;
    public static final String VERSION_STRING = VERSION_MAJOR+"."+VERSION_MINOR+"."+VERSION_PATCH;

    public static final OiModule moduleStd = new LibStd();
    public static final OiModule moduleMath = new LibMath();

    public static Script load(String filename, String source, OiObject globals) throws ParsingException {
        return load(new Source(source, filename), globals, extractScripts(globals));
    }

    public static Script load(String filename, String source, OiObject globals, OiObject scripts) throws ParsingException {
        return load(new Source(source, filename), globals, scripts);
    }

    public static Script loadAndInit(String filename, String source, OiObject globals) throws ParsingException {
        return loadAndInit(filename, source, globals, extractScripts(globals));
    }

    public static Script loadAndInit(String filename, String source, OiObject globals, OiObject scripts) throws ParsingException {
        Script script = load(filename, source, globals, scripts);
        script.init();
        return script;
    }

    public static Script load(Source source, OiObject globals, OiObject scripts) throws ParsingException {
        Parser parser = new Parser();
        Script script = parser.perform(source);
        script.include(globals);
        if (globals.has("std"))
            script.include((OiObject) globals.get("std"));
        if (scripts != null) {
            scripts.set(source.getFilename().substring(0, source.getFilename().lastIndexOf(".oi")), script);
        }
        script.prepare();
        return script;
    }

    static final Parser evalParser = new Parser();
    static final Script evalScript = new Script("<eval>", new HashMap<>(), new HashMap<>(), new ArrayList<>());
    static {
        evalScript.include(new LibStd());
        evalScript.include(new LibMath());
    }
    static final Context emptyContext = new Context(evalScript, null);
    public static Object eval(String code) throws ParsingException {
        synchronized (evalParser) {
            evalParser.setSource(new Source(code, "<eval>"));
            Value value = evalParser.parseValue(0);
            Object result = value.eval(emptyContext);
            emptyContext.namespace.clear();
            return result;
        }
    }

    public static Object eval(String code, Map<String, Object> args) throws ParsingException {
        synchronized (evalParser) {
            emptyContext.namespace.putAll(args);
            return eval(code);
        }
    }

    public static OiObject extractScripts(OiObject globals) {
        Object object = globals.get("std");
        if (object == null)
            return null;
        OiModule module = (OiModule) object;
        object = module.get("_scripts");
        if (object == null)
            return null;
        return (OiObject) object;
    }
}
