package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.jit.JitCompiler;
import mihailris.oiscript.runtime.OiExecutable;
import mihailris.oiscript.runtime.OiRunHandle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AsmTest {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException, ParsingException {
        JitCompiler jitCompiler = new JitCompiler();

        OiObject globals = new OiObject();
        globals.set("std", OI.moduleStd);
        globals.set("math", OI.moduleMath);
        Script script = OI.loadAndInit("stdext.oi", new String(Files.readAllBytes(new File("jit-test.oi").toPath())), globals);
        RawFunction function = (RawFunction) script.get("square");
        System.out.println(function);

        long tm = System.currentTimeMillis();
        OiExecutable executable = jitCompiler.compile(function);
        System.out.println(executable+" "+(System.currentTimeMillis()-tm)+" ms");
        tm = System.currentTimeMillis();
        Object result = executable.execute(new Context(script, new OiRunHandle(), 1), 10_000_000);
        System.out.println(result+" in "+(System.currentTimeMillis()-tm)+" ms");
    }
}
