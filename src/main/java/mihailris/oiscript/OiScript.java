package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.parsing.Parser;

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
}
