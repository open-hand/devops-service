package io.choerodon.devops.domain.application.valueobject;

public class Variable {

    private String key;
    private String value;

    public Variable() {
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
