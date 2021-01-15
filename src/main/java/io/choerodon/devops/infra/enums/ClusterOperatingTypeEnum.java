package io.choerodon.devops.infra.enums;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/26 17:48
 */
public enum ClusterOperatingTypeEnum {
    INSTALL_CLUSTER("install_cluster"),
    DELETE_CLUSTER("delete_cluster"),
    ADD_NODE("add_node"),
    DELETE_NODE("delete_node"),
    DELETE_NODE_ROLE("delete_node_role");


    private String value;

    ClusterOperatingTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
