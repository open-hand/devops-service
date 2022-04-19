//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.choerodon.devops.infra.dto.gitlab;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Variable {
    private String key;
    private String value;
    private String variableType;
    @JsonProperty("protected")
    private Boolean isProtected;
    @JsonProperty("masked")
    private Boolean isMasked;
    private String environmentScope;

    public Variable() {
    }

    public Variable(String key, String value) {
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

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public Boolean getProtected() {
        return this.isProtected;
    }

    public void setProtected(Boolean isProtected) {
        this.isProtected = isProtected;
    }

    public Boolean getMasked() {
        return this.isMasked;
    }

    public void setMasked(Boolean masked) {
        this.isMasked = masked;
    }

    public String getEnvironmentScope() {
        return this.environmentScope;
    }

    public void setEnvironmentScope(String environmentScope) {
        this.environmentScope = environmentScope;
    }
}
