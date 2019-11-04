package io.choerodon.devops.infra.util;

import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.infra.enums.C7NHelmReleaseMetadataType;
import io.choerodon.devops.infra.enums.EnvironmentType;

/**
 * 集群相关组件的工具类
 *
 * @author zmf
 * @since 11/4/19
 */
public class ClusterComponentUtil {
    private ClusterComponentUtil() {
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
}
