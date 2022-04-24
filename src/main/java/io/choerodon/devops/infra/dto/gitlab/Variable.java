//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.choerodon.devops.infra.dto.gitlab;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Variable {
    private String key;
    private String value;
    private Variable.Type variableType;
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

    public Variable.Type getVariableType() {
        return this.variableType;
    }

    public void setVariableType(Variable.Type variableType) {
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

    public static final List<Variable> convertMapToList(Map<String, String> variables) {
        if (variables == null) {
            return null;
        } else {
            List<Variable> varList = new ArrayList(variables.size());
            variables.forEach((k, v) -> {
                varList.add(new Variable(k, v));
            });
            return varList;
        }
    }

    public static enum Type {
        ENV_VAR,
        FILE;

        private static JacksonJsonEnumHelper<Variable.Type> enumHelper = new JacksonJsonEnumHelper(Variable.Type.class);

        private Type() {
        }

        @JsonCreator
        public static Variable.Type forValue(String value) {
            return (Variable.Type)enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return enumHelper.toString(this);
        }

        public String toString() {
            return enumHelper.toString(this);
        }
    }
}
