package mihailris.oiscript.exceptions;

import mihailris.oiscript.Script;

public class MemberNotDefinedException extends RuntimeException {
    private final Script script;
    private final String name;

    public MemberNotDefinedException(Script script, String name, String message) {
        super(message);
        this.script = script;
        this.name = name;
    }
}
