package io.choerodon.devops.infra.enums;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/19
 * @Modified By:
 */
public enum AppCenterDeployObjectEnum {
    CHART("chart", "char部署"),
    DEPLOY_GROUP("deploy_group", "部署组");

    private String code;
    private String value;

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    AppCenterDeployObjectEnum(String code, String value) {
        this.value = value;
        this.code = code;
    }
}
