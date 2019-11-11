package io.choerodon.devops.infra.enums;

/**
 * @author zhaotianxin
 * @since 2019/11/1
 */
public enum ClusterResourceOperateType {
    INSTALL("install"),
    UPGRADE("upgrade"),
    UNINSTALL("uninstall");
    private String type;

    ClusterResourceOperateType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
