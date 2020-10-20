package io.choerodon.devops.infra.enums;

/**
 * 节点类型
 */
public enum ClusterNodeType {
    /**
     * worker节点
     */
    WORKER("worker"),
    /**
     * etcd节点
     */
    ETCD("etcd"),
    /**
     * master节点
     */
    MASTER("master");

    private String type;

    ClusterNodeType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    /**
     * 包含master，返回true
     *
     * @param types 类型
     * @return boolean
     */
    public static boolean isMaster(String types) {
        return types.contains(MASTER.getType());
    }

    /**
     * 包含etcd，返回true
     *
     * @param types 类型
     * @return boolean
     */
    public static boolean isEtcd(String types) {
        return types.contains(ETCD.getType());
    }

    /**
     * 包含worker，返回true
     *
     * @param types 类型
     * @return boolean
     */
    public static boolean isWorker(String types) {
        return types.contains(WORKER.getType());
    }

    /**
     * flag 等于3返回true，表示etcd节点和worker节点
     *
     * @param types 类型
     * @return boolean
     */
    public static boolean isEtcdAndWorker(String types) {
        return types.contains(ETCD.getType()) && types.contains(WORKER.getType());
    }

    /**
     * flag 等于6返回true，表示master、etcd节点
     *
     * @param types 类型
     * @return boolean
     */
    public static boolean isMasterAndEtcd(String types) {
        return types.contains(MASTER.getType()) && types.contains(ETCD.getType());
    }

    /**
     * flag 等于5返回true，表示master、worker节点
     *
     * @param types 类型
     * @return boolean
     */
    public static boolean isMasterAndWorker(String types) {
        return types.contains(MASTER.getType()) && types.contains(WORKER.getType());
    }

    /**
     * flag 等于7返回true，表示master、etcd、worker节点
     *
     * @param types 类型
     * @return boolean
     */
    public static boolean isMasterAndEtcdAndWorker(String types) {
        return types.contains(MASTER.getType()) && types.contains(ETCD.getType()) && types.contains(WORKER.getType());
    }
}
