package io.choerodon.devops.infra.util;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.infra.enums.C7NHelmReleaseMetadataType;
import io.choerodon.devops.infra.enums.EnvironmentType;

/**
 * GitOps解析相关的工具类
 *
 * @author zmf
 * @since 11/4/19
 */
public class GitOpsUtil {
    private GitOpsUtil() {
    }

    /**
     * 实例是否是集群组件的实例
     *
     * @param envType        环境类型
     * @param c7nHelmRelease release数据
     * @return true则是，反之，不是
     */
    public static boolean isClusterComponent(String envType, C7nHelmRelease c7nHelmRelease) {
        return EnvironmentType.SYSTEM.getValue().equals(envType) && C7NHelmReleaseMetadataType.CLUSTER_COMPONENT.getType().equals(c7nHelmRelease.getMetadata().getType());
    }

    /**
     * 根据资源名称对资源进行分拣处理，从所有涉及的资源分拣出哪些是新增的，更新的和删除的
     *
     * @param beforeResourceNames 此处操作前数据库的所有资源名称
     * @param all                 所有涉及的资源，分类之后这个列表中存放的是待删除的资源
     * @param add                 放置新增的资源的容器，分类之后将需要更新的资源放入此处，建议传入时为空
     * @param update              放置更新的资源的容器，分类之后将需要更新的资源放入此处，建议传入时为空
     * @param getName             获取资源的名称的逻辑
     */
    public static <T> void pickCUDResource(List<String> beforeResourceNames,
                                           List<T> all,
                                           List<T> add,
                                           List<T> update,
                                           Function<T, String> getName) {
        Iterator<T> iterator = all.iterator();
        while (iterator.hasNext()) {
            T obj = iterator.next();
            if (beforeResourceNames.contains(getName.apply(obj))) {
                update.add(obj);
                iterator.remove();
            } else {
                add.add(obj);
            }
        }
    }
}
