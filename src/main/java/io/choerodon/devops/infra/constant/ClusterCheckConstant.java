package io.choerodon.devops.infra.constant;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/10/21 10:07
 */
public class ClusterCheckConstant {
    // cluster
    public static final String ERROR_CLUSTER_ID_IS_NULL = "error.cluster.id.is.null";
    public static final String ERROR_CLUSTER_STATUS_IS_OPERATING = "error.cluster.status.is.operating";
    public static final String ERROR_CLUSTER_TYPE_IS_OPERATING = "error.cluster.type.is.operating";

    public static final String ERROR_NODE_ID_IS_NULL = "error.node.id.is.null";
    public static final String ERROR_NODE_NAME_IS_NULL = "error.node.name.is.null";
    public static final String ERROR_ROLE_ID_IS_NULL = "error.role.id.is.null";
    public static final String ERROR_DELETE_NODE_FAILED = "error.delete.node.failed";
    public static final String ERROR_DELETE_NODE_ROLE_FAILED = "error.delete.node.role.failed";

    public static final String ERROR_WORKER_NODE_ONLY_ONE = "error.worker.node.only.one";
    public static final String ERROR_MASTER_NODE_ONLY_ONE = "error.master.node.only.one";
    public static final String ERROR_ETCD_NODE_ONLY_ONE = "error.etcd.node.only.one";

}
