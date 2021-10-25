package ru.akirakozov.sd.refactoring.html;

public class PageBuilder {
    public PageBuilder() {
        sb = new StringBuilder();
        appendLine("<html><body>");
    }

    public <T> PageBuilder append(T value) {
        sb.append(value);
        return this;
    }

    public <T> PageBuilder appendLine(T value) {
        return append(value).nl();
    }

    public PageBuilder nl() {
        return append(System.lineSeparator());
    }

    public PageBuilder appendHeader(String header, int level) {
        return append("<h").append(level).append(">").append(header).append("</h").append(level).appendLine(">");
    }

    public PageBuilder appendHeader1(String header) {
        return appendHeader(header, 1);
    }

    public PageBuilder appendProductInfo(String name, int value) {
        return append(name).append("\t").append(value).appendLine("</br>");
    }

    public String toString() {
        append("</body></html>");
        return sb.toString();
    }

    private StringBuilder sb;
}
