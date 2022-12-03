package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.parsing.Parser;
import mihailris.oiscript.parsing.Value;

public class OiScript {
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

    public static Object eval(String code) throws ParsingException {
        Parser parser = new Parser();
        parser.setSource(new Source(code, "<eval>"));
        Value value = parser.parseValue(0);
        return value.eval(new Context(null, null));
    }
}
