package io.choerodon.devops.domain.application.valueobject;

/**
 * @author crockitwood
 */
public class HighlightMarker {
    private int line;
    private int startIndex;
    private int endIndex;
    private int startColumn;
    private int endColumn;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    @Override
    public String toString() {
        return "HighlightMarker{" +
                "line=" + line +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                ", startColumn=" + startColumn +
                ", endColumn=" + endColumn +
                '}';
    }
}
