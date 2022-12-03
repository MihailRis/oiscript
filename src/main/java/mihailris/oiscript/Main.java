package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.runtime.OiRunHandle;
import mihailris.oiscript.stdlib.LibMath;
import mihailris.oiscript.stdlib.LibStd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {
        String sourceCode = new String(Files.readAllBytes(new File("test.oi").toPath()));
        try {
            OiObject globals = new OiObject();
            globals.set("std", new LibStd());
            globals.set("math", new LibMath());

            Script script = OiScript.load("test.oi", sourceCode, globals);
            System.out.println(script);
            System.out.println("========= Runtime =========");
            long tm = System.currentTimeMillis();
            script.execute("init");
            OiRunHandle handle = script.start("test_proc");
            while (!handle.isFinished()){
                //System.out.println("...");
                handle.continueProc();
            }
            System.out.println("\n=========   End   =========");
            System.out.println((System.currentTimeMillis() - tm)+" ms");

            System.out.println(OiScript.eval("5 * 3"));
        } catch (ParsingException e) {
            e.printOiErrorTrace();
            e.printStackTrace();
        }
    }
}
