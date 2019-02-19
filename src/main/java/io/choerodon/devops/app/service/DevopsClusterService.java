package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsClusterService {

    String createCluster(Long organizationId, DevopsClusterReqDTO devopsClusterReqDTO);

    void updateCluster(Long clusterId, DevopsClusterReqDTO devopsClusterReqDTO);

    void checkName(Long organizationId, String name);

    Page<ProjectDTO> listProjects(Long organizationId, Long clusterId, PageRequest pageRequest, String[] params);

    String queryShell(Long clusterId);

    void checkCode(Long organizationId, String code);

    Page<ClusterWithNodesDTO> pageClusters(Long organizationId, Boolean doPage, PageRequest pageRequest, String params);

    List<ProjectDTO> listClusterProjects(Long organizationId, Long clusterId);

    String deleteCluster(Long clusterId);

    DevopsClusterRepDTO getCluster(Long clusterId);

    /**
     * 分页查询节点下的Pod
     * @param clusterId 集群id
     * @param nodeName 节点名称
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    Page<DevopsClusterPodDTO> pageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam);


}
