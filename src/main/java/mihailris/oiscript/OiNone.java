package mihailris.oiscript;

public class OiNone {
    public static final OiNone NONE = new OiNone();
    private OiNone(){
    }

    @Override
    public String toString() {
        return "none";
    }
}
