package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsClusterRepDTO;
import io.choerodon.devops.api.dto.DevopsClusterReqDTO;
import io.choerodon.devops.api.dto.ProjectDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsClusterService {

    String createCluster(Long organizationId, DevopsClusterReqDTO devopsClusterReqDTO);

    void updateCluster(Long clusterId, DevopsClusterReqDTO devopsClusterReqDTO);

    void checkName(Long organizationId, String name);

    Page<ProjectDTO> listProjects(Long organizationId, Long clusterId, PageRequest pageRequest, String[] params);

    String queryShell(Long clusterId);

    void checkCode(Long organizationId, String code);

    Page<DevopsClusterRepDTO> pageClusters(Long organizationId, Boolean doPage, PageRequest pageRequest, String params);

    List<ProjectDTO> listClusterProjects(Long organizationId, Long clusterId);

    String deleteCluster(Long clusterId);

    DevopsClusterRepDTO getCluster(Long clusterId);
}
