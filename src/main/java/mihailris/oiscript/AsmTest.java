package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.jit.JitCompiler;
import mihailris.oiscript.runtime.OiExecutable;
import mihailris.oiscript.runtime.OiRunHandle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

        Random random = new Random();
        List<Object> numbers = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            numbers.add((long)random.nextInt(1000));
        }
        tm = System.currentTimeMillis();
        Object result = executable.execute(new Context(script, new OiRunHandle(), 1), numbers);
        System.out.println(result+" ["+result.getClass().getSimpleName()+"] in "+(System.currentTimeMillis()-tm)+" ms");
    }
}
