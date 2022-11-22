package io.choerodon.devops.infra.enums;

public enum DevopsCiSonarGateConditionScopeEnum {
    /**
     * 所有代码
     */
    ALL("all"),

    /**
     * 新增代码
     */
    NEW("new");

    private String scope;

    DevopsCiSonarGateConditionScopeEnum(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return this.scope;
    }

    public static DevopsCiSonarGateConditionScopeEnum parseScope(String metric) {
        if (metric.startsWith("new_")) {
            return NEW;
        } else {
            return ALL;
        }
    }
}
