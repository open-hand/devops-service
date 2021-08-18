package io.choerodon.devops.infra.enums;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
public enum AppCenterRdupmTypeEnum {
    CHART("chart"),
    JAR("jar"),
    DOCKER("docker");

    private String type;

    public String getType() {
        return type;
    }

    AppCenterRdupmTypeEnum(String type) {
        this.type = type;
    }
}
