package io.choerodon.devops.api.vo.kubernetes;

/**
 * 标记高亮
 *
 * @author crockitwood
 */
public class HighlightMarker {
    private int line;
    private int endLine;
    private int startIndex;
    private int endIndex;
    private int startColumn;
    private int endColumn;

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

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
