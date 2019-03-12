package io.choerodon.devops.infra.common.util.enums;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/12
 */
public enum ProjectConfigType {
    HARBOR("harbor"),
    CHART("chart");

    private String type;

    ProjectConfigType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
