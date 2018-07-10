package io.choerodon.devops.domain.application.valueobject;

import org.yaml.snakeyaml.nodes.Node;

/**
 * @author crcokitwood
 */
public class InsertNode {

    private int line ;
    private  int startColumn;
    private String key;
    private int lastIndex;
    private Node value;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Node getValue() {
        return value;
    }

    public void setValue(Node value) {
        this.value = value;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }
}
