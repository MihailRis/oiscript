package mihailris.oiscript.parsing;

public class Position {
    int line;
    int linepos;
    int pos;

    public Position() {
    }

    public Position(int line, int linepos, int pos) {
        this.line = line;
        this.linepos = linepos;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return (line+1)+":"+getLocalPos();
    }

    public Position cpy() {
        return new Position(line, linepos, pos);
    }

    public void reset() {
        line = 0;
        linepos = 0;
        pos = 0;
    }

    public void newline() {
        linepos = pos;
        line++;
    }

    public int getLine() {
        return line;
    }

    public int getLocalPos() {
        return pos - linepos;
    }

    public int getLinePos() {
        return linepos;
    }

    public void set(Position position) {
        this.pos = position.pos;
        this.line = position.line;
        this.linepos = position.linepos;
    }
}
