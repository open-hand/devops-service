package io.choerodon.devops.infra.enums;

/**
 * @author zhaotianxin
 * @since 2019/10/30
 */
public enum ClusterResourceType {
    PROMETHEUS("prometheus"),
    CERTMANAGER("cert-manager");

    private String type;

    ClusterResourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
