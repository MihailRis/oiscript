package mihailris.oiscript.jit;

import mihailris.oiscript.runtime.Function;
import org.objectweb.asm.Label;

public class CompilerContext {
    private final CompilerContext parent;
    private final Function function;
    private int localsCount;
    private final Label loopStart;
    private final Label loopEnd;

    public CompilerContext(Function function) {
        this.parent = null;
        this.function = function;
        this.localsCount = function.getLocals().size();
        this.loopStart = null;
        this.loopEnd = null;
    }

    private CompilerContext(CompilerContext parent, Label loopStart, Label loopEnd) {
        this.parent = parent;
        this.function = parent.function;
        this.loopStart = loopStart;
        this.loopEnd = loopEnd;
    }

    public Label getLoopStart() {
        return loopStart;
    }

    public Label getLoopEnd() {
        return loopEnd;
    }

    public CompilerContext loop(Label loopStart, Label loopEnd) {
        return new CompilerContext(this, loopStart, loopEnd);
    }

    public int newLocal() {
        if (parent != null)
            return parent.newLocal();
        return localsCount++;
    }

    public Function getFunction() {
        return function;
    }

}
