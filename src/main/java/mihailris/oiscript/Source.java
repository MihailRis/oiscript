package mihailris.oiscript;

public class Source {
    private String source;
    private String filename;
    public Source(String source, String filename) {
        this.source = source;
        this.filename = filename;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
