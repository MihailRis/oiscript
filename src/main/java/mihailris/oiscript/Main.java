package mihailris.oiscript;

import mihailris.oiscript.exceptions.OiRuntimeException;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.runtime.OiRunHandle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    @SuppressWarnings("ConstantConditions")
    public static void main(String[] args) throws IOException {
        System.out.println("OiScript v"+ OI.VERSION_STRING+" © MihailRis 2022-2023");
        String sourceCode = new String(Files.readAllBytes(new File("test.oi").toPath()));
        boolean verbose = true;
        try {
            OiObject globals = new OiObject();
            OiObject scripts = new OiObject();
            globals.set("std", OI.moduleStd);
            globals.set("math", OI.moduleMath);
            globals.set("stdext", OI.loadAndInit("stdext.oi", new String(Files.readAllBytes(new File("stdext.oi").toPath())), globals, scripts));
            Script script = OI.load("test.oi", sourceCode, globals, scripts);
            script.init();
            if (script.has("run")) {
                System.out.println("========= Runtime =========");
                long tm = System.currentTimeMillis();
                script.execute("run");
                System.out.println("\n=========   End   =========");
                System.out.println((System.currentTimeMillis() - tm)+" ms");
            }
            if (script.has("scene")) {
                System.out.println("========= Process ==========");
                long tm = System.currentTimeMillis();
                OiRunHandle runHandle = script.start("scene");
                while (!runHandle.isFinished()) {
                    System.out.println("<step>");
                    runHandle.continueProc();
                }
                System.out.println("\n=========   End   =========");
                System.out.println((System.currentTimeMillis() - tm)+" ms");
            }
        } catch (ParsingException e) {
            e.printOiErrorTrace();
            if (verbose) e.printStackTrace();
        } catch (OiRuntimeException e) {
            e.printOiErrorTrace();
            if (verbose) e.printStackTrace();
        }
    }
}
