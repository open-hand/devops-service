package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author lihao
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CiVariableVO {

    private String key;
    private String value;

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

    @Override
    public String toString() {
        return "CiVariableVO{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
