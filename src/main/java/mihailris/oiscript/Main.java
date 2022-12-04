package mihailris.oiscript;

import mihailris.oiscript.exceptions.OiRuntimeException;
import mihailris.oiscript.exceptions.ParsingException;
import mihailris.oiscript.stdlib.LibMath;
import mihailris.oiscript.stdlib.LibStd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("OiScript v"+OiScript.VERSION_STRING+" © MihailRis 2022");
        String sourceCode = new String(Files.readAllBytes(new File("test.oi").toPath()));
        boolean verbose = false;
        try {
            OiObject globals = new OiObject();
            globals.set("std", new LibStd());
            globals.set("math", new LibMath());
            globals.extend((OiObject) globals.get("std"));

            Script script = OiScript.load("test.oi", sourceCode, globals);
            if (script.has("run")) {
                System.out.println("========= Runtime =========");
                long tm = System.currentTimeMillis();
                script.execute("run");
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
