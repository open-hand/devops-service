package io.choerodon.devops.api.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author lihao
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CiVariableVO {

    @ApiModelProperty("key")
    private String key;
    @ApiModelProperty("value")
    private String value;

    public CiVariableVO() {
    }

    public CiVariableVO(String key, String value) {
        this.key = key;
        this.value = value;
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

    @Override
    public String toString() {
        return "CiVariableVO{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
