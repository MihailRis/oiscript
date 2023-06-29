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
        for (int j = 0; j < 1; j++) {
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
            Context context = new Context(script, new OiRunHandle(), 1);
            tm = System.nanoTime();
            Object result = executable.execute(context, numbers);
            long spent = System.nanoTime() - tm;
            System.out.println("OI [" + result.getClass().getSimpleName() + "] in " + spent / 1000_000.0 + " ms");
        }
    }
}
