package io.choerodon.devops.api.vo.kubernetes;

/**
 * 文件替换
 *
 * @author crockitwood
 */
public class ReplaceMarker {
    private int line;
    private int startIndex;
    private int endIndex;
    private int startColumn;
    private int endColumn;

    private String toReplace;

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

    public String getToReplace() {
        return toReplace;
    }

    public void setToReplace(String toReplace) {
        this.toReplace = toReplace;
    }

    @Override
    public String toString() {
        return "ReplaceMarker{"
                + "line=" + line
                + ", startIndex=" + startIndex
                + ", endIndex=" + endIndex
                + ", startColumn=" + startColumn
                + ", endColumn=" + endColumn
                + ", toReplace='" + toReplace + '\''
                + '}';
    }
}
