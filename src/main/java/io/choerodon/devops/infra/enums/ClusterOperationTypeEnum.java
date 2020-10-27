package io.choerodon.devops.infra.enums;

/**
 * 操作类型
 */
public enum ClusterOperationTypeEnum {
    /**
     * 安装k8s
     */
    INSTALL_K8S("install_k8s"),
    /**
     * 卸载k8s
     */
    UNINSTALL_K8S("uninstall_k8s"),
    /**
     * 添加master节点
     */
    ADD_MASTER("add_master"),
    /**
     * 添加worker节点
     */
    ADD_WORKER("add_worker"),
    /**
     * 删除master节点
     */
    DEL_MASTER("del_master"),
    /**
     * 删除worker节点
     */
    DEL_WORKER("del_worker"),
    /**
     * 删除节点
     */
    DEL_NODE("del_node"),
    /**
     * 添加节点
     */
    ADD_NODE("add_node");

    ClusterOperationTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }
}
