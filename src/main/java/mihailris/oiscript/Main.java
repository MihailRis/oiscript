package mihailris.oiscript;

import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.runtime.OiRunHandle;
import mihailris.oiscript.stdlib.LibMath;
import mihailris.oiscript.stdlib.LibStd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("OiScript v"+OiScript.VERSION_STRING+" © MihailRis 2022");
        String sourceCode = new String(Files.readAllBytes(new File("test.oi").toPath()));
        try {
            OiObject globals = new OiObject();
            globals.set("std", new LibStd());
            globals.set("math", new LibMath());

            Script script = OiScript.load("test.oi", sourceCode, globals);
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

            System.out.println(OiScript.eval("rand()"));
            System.out.println(OiScript.eval("a * 100", Collections.singletonMap("a", 4)));
        } catch (ParsingException e) {
            e.printOiErrorTrace();
            e.printStackTrace();
        }
    }
}
