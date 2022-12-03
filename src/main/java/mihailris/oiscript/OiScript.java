package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.parsing.Parser;
import mihailris.oiscript.parsing.Value;
import mihailris.oiscript.stdlib.LibMath;
import mihailris.oiscript.stdlib.LibStd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OiScript {
    public static final int VERSION_MAJOR = 1;
    public static final int VERSION_MINOR = 0;
    public static final int VERSION_PATCH = 0;
    public static final String VERSION_STRING = VERSION_MAJOR+"."+VERSION_MINOR+"."+VERSION_PATCH;

    public static Script load(String filename, String source, OiObject globals) throws ParsingException {
        return load(new Source(source, filename), globals);
    }

    public static Script load(Source source, OiObject globals) throws ParsingException {
        Parser parser = new Parser();
        Script script = parser.perform(source);
        script.extend(globals);
        script.prepare();
        if (script.get("init") != null){
            script.execute("init");
        }
        return script;
    }

    static final Parser evalParser = new Parser();
    static final Script evalScript = new Script("<eval>", new HashMap<>(), new HashMap<>(), new ArrayList<>());
    static {
        evalScript.extend(new LibStd());
        evalScript.extend(new LibMath());
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
}
