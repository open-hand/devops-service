package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;
import io.choerodon.devops.infra.enums.ClusterResourceType;

/**
 * 为集群组件创建对应的Release
 *
 * @author zmf
 * @since 10/29/19
 */
public interface ComponentReleaseService {
    /**
     * 为Prometheus组件创建相应的release
     *
     * @param systemEnvId         用于部署组件对应实例的系统环境
     * @param devopsPrometheusDTO 组件信息
     * @return 组件对应的实例纪录
     */
    AppServiceInstanceDTO createReleaseForPrometheus(Long systemEnvId, DevopsPrometheusDTO devopsPrometheusDTO);

    /**
     * 更新Prometheus实例
     *
     * @param devopsPrometheusDTO 用于更新组件的对应信息
     * @param instanceId          组件对应的实例id
     * @param systemEnvId         集群对应的环境id
     * @return 组件更新后的实例纪录
     */
    AppServiceInstanceDTO updateReleaseForPrometheus(DevopsPrometheusDTO devopsPrometheusDTO, Long instanceId, Long systemEnvId);

    /**
     * 重试将实例推向GitLab
     *
     * @param instanceId          实例id
     * @param clusterResourceType 集群组件类型
     */
    boolean retryPushingToGitLab(Long instanceId, ClusterResourceType clusterResourceType);

    /**
     * 重启组件对应的实例
     *
     * @param instanceId          实例id
     * @param clusterResourceType 集群组件类型
     */
    boolean restartComponentInstance(Long instanceId, ClusterResourceType clusterResourceType);


    /**
     * 删除组件对应的实例
     *
     * @param instanceId 组件对应的实例ID
     */
    void deleteReleaseForComponent(Long instanceId, Boolean deletePrometheus);
}
