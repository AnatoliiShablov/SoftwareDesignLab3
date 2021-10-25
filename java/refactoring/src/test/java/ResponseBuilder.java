public class ResponseBuilder {
    public ResponseBuilder() {
        sb = new StringBuilder();
    }

    public ResponseBuilder append(String str) {
        sb.append(str);
        return this;
    }

    public ResponseBuilder appendLine(String str) {
        sb.append(str);
        return this.nl();
    }


    public ResponseBuilder nl() {
        sb.append(System.lineSeparator());
        return this;
    }

    public String toString() {
        return sb.toString();
    }

    private final StringBuilder sb;

}
