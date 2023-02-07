package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OiScriptTest {
    @Test
    public void evalTest() throws ParsingException {
        assertEquals(OI.eval("20 - 5 * 3.2"), 4.0);
        assertEquals(OI.eval("2 * 3 == 6"), true);
        assertTrue(OI.eval("[]") instanceof List);
        assertEquals(OI.eval("sqrt(81)"), 9.0);
    }

    @Test
    public void scriptTest() throws ParsingException {
        String sourceCode =
                "a = 5\n" +
                "b = 12\n" +
                "if a > b:\n" +
                "   print(a)\n" +
                "else:\n" +
                "   print(b)\n";
        OiObject globals = new OiObject();
        OiObject scripts = new OiObject();
        Script script = OI.load("test.oi", sourceCode, globals, scripts);
    }
}