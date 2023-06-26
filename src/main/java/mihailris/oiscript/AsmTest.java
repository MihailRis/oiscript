package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.jit.JitCompiler;
import mihailris.oiscript.runtime.Function;
import mihailris.oiscript.runtime.OiExecutable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class AsmTest {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException, ParsingException {
        JitCompiler jitCompiler = new JitCompiler();

        OiObject globals = new OiObject();
        globals.set("std", OI.moduleStd);
        globals.set("math", OI.moduleMath);
        Script script = OI.loadAndInit("stdext.oi", new String(Files.readAllBytes(new File("jit-test.oi").toPath())), globals);
        Function function = (Function) script.get("square");
        System.out.println(function);

        OiExecutable executable = jitCompiler.compile(function);
        System.out.println(executable);
    }
}
