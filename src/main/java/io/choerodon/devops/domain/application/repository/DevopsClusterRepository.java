package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvPodDTO;
import io.choerodon.devops.domain.application.entity.DevopsClusterE;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsClusterRepository {

    DevopsClusterE create(DevopsClusterE devopsClusterE);

    void checkName(DevopsClusterE devopsClusterE);

    void checkCode(DevopsClusterE devopsClusterE);

    List<DevopsClusterE> listByProjectId(Long projectId, Long organizationId);

    DevopsClusterE query(Long clusterId);

    void update(DevopsClusterE devopsClusterE);

    Page<DevopsClusterE> pageClusters(Long organizationId, Boolean doPage, PageRequest pageRequest, String params);

    void delete(Long clusterId);

    DevopsClusterE queryByToken(String token);

    List<DevopsClusterE> list();

    /**
     * 分页查询节点下的Pod
     * @param clusterId 集群id
     * @param nodeName 节点名称
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    Page<DevopsEnvPodE> pageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam);

    DevopsClusterE queryByCode(Long organizationId, String code);
}
