package io.choerodon.devops.infra.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 节点角色
 * 使用掩码来表示多个关系 abc
 * a表示master节点
 * b表示etcd节点
 * c表示worker节点
 */
public enum ClusterNodeRoleEnum implements Type {
    /**
     * worker节点
     */
    WORKER,
    /**
     * etcd节点
     */
    ETCD,
    /**
     * master节点
     */
    MASTER;

    @Override
    public int getMask() {
        return (1 << ordinal());
    }

    @Override
    public boolean isSet(int flags) {
        return (flags & getMask()) != 0;
    }

    public static int collectDefaults() {
        int flags = 0;
        for (ClusterNodeRoleEnum t : values()) {
            flags |= t.getMask();
        }
        return flags;
    }

    /**
     * flag 等于4返回true，表示主节点
     *
     * @param flag 标志
     * @return boolean
     */
    public static boolean isMaster(int flag) {
        return MASTER.isSet(flag);
    }

    /**
     * flag 等于2返回true，表示etcd节点
     *
     * @param flag 标志
     * @return boolean
     */
    public static boolean isEtcd(int flag) {
        return ETCD.isSet(flag);
    }

    /**
     * flag 等于1返回true，表示worker节点
     *
     * @param flag 标志
     * @return boolean
     */
    public static boolean isWorker(int flag) {
        return WORKER.isSet(flag);
    }

    /**
     * flag 等于3返回true，表示etcd节点和worker节点
     *
     * @param flag 标志
     * @return boolean
     */
    public static boolean isEtcdAndWorker(int flag) {
        return ETCD.isSet(flag) && WORKER.isSet(flag);
    }

    /**
     * flag 等于6返回true，表示master、etcd节点
     *
     * @param flag 标志
     * @return boolean
     */
    public static boolean isMasterAndEtcd(int flag) {
        return MASTER.isSet(flag) && ETCD.isSet(flag);
    }

    /**
     * flag 等于5返回true，表示master、worker节点
     *
     * @param flag 标志
     * @return boolean
     */
    public static boolean isMasterAndWorker(int flag) {
        return MASTER.isSet(flag) && WORKER.isSet(flag);
    }

    /**
     * flag 等于7返回true，表示master、etcd、worker节点
     *
     * @param flag 标志
     * @return boolean
     */
    public static boolean isMasterAndEtcdAndWorker(int flag) {
        return MASTER.isSet(flag) && ETCD.isSet(flag) && WORKER.isSet(flag);
    }

    public static Set<Integer> listMasterRoleSet() {
        return new HashSet<>(Arrays.asList(4, 5, 6));
    }

    public static Set<Integer> listEtcdRoleSet() {
        return new HashSet<>(Arrays.asList(2, 3, 6));
    }

    public static Set<Integer> listWorkerRoleSet() {
        return new HashSet<>(Arrays.asList(1, 3, 5, 7));
    }
}
