package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsProjectOverview;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.agile.SprintDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.AgileServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DevopsProjectOverviewImpl implements DevopsProjectOverview {

    private static final String UP = "up";
    private static final String DOWN = "down";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;


    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private AgileServiceClientOperator agileServiceClientOperator;

    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;

    @Autowired
    private AppServiceMapper appServiceMapper;

    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper;

    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;

    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;

    @Override
    public Map<String, Long> getEnvStatusCount(Long projectId) {

        Map<String, Long> count = new HashMap<>();

        List<Long> updatedClusterList = clusterConnectionHandler.getUpdatedClusterList();

        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentMapper.listByProjectId(projectId);

        count.put(UP, devopsEnvironmentDTOS.stream()
                .filter(t -> isEnvUp(updatedClusterList, t))
                .count());

        count.put(DOWN, devopsEnvironmentDTOS.stream()
                .filter(t -> !isEnvUp(updatedClusterList, t))
                .count());

        return count;
    }

    @Override
    public Map<String, Long> getAppServiceStatusCount(Long projectId) {
        Map<String, Long> count = new HashMap<>();

        List<AppServiceDTO> appServiceDTOList = appServiceMapper.listByProjectId(projectId, null, null);

        count.put(UP, appServiceDTOList.stream()
                .filter(AppServiceDTO::getSynchro)
                .filter(a -> !a.getFailed())
                .filter(AppServiceDTO::getActive)
                .count());

        count.put(DOWN, appServiceDTOList.stream()
                .filter(AppServiceDTO::getSynchro)
                .filter(a -> !a.getFailed())
                .filter(t -> !t.getActive())
                .count());

        return count;
    }

    @Override
    public Map<String, Long> getCommitCount(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        SprintDTO sprintDTO = agileServiceClientOperator.getActiveSprint(projectId, projectDTO.getOrganizationId());
        List<Date> dateList = devopsGitlabCommitMapper.queryCountByProjectIdAndCreationDate(projectId, sprintDTO.getStartDate());

        return dateList.stream().map(simpleDateFormat::format)
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
    }

    @Override
    public Map<String, Long> getDeployCount(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        // 该项目下所有启用环境
        List<Long> updatedClusterList = clusterConnectionHandler.getUpdatedClusterList();

        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentMapper.listByProjectId(projectId);

        List<Long> activeEnvIds = devopsEnvironmentDTOS.stream()
                .filter(t -> isEnvUp(updatedClusterList, t))
                .map(DevopsEnvironmentDTO::getId)
                .collect(Collectors.toList());

        // 查出本项目下的所有应用服务
        Map<String, Object> searchParam = new HashMap<>();
        searchParam.put("active", 1);
        List<Long> appServiceIds = appServiceMapper.listByProjectId(projectId, searchParam, null).stream().map(AppServiceDTO::getId).collect(Collectors.toList());

        // 根据启用环境查出所有instance

        SprintDTO sprintDTO = agileServiceClientOperator.getActiveSprint(projectId, projectDTO.getOrganizationId());

        List<Long> instanceIds = appServiceInstanceMapper.queryInstanceByAppServiceIdsAndStartDate(appServiceIds).stream()
                .filter(i -> activeEnvIds.contains(i.getEnvId()))
                .map(AppServiceInstanceDTO::getId)
                .collect(Collectors.toList());

        // 查询从自迭代开启到现在,每个实例的部署操作记录

        List<DevopsEnvCommandDTO> instance = devopsEnvCommandMapper.listByInstanceIdsAndStartDate("instance", instanceIds, sprintDTO.getStartDate());

        return instance.stream().map(simpleDateFormat::format)
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
    }

    private boolean isEnvUp(List<Long> updatedClusterList, DevopsEnvironmentDTO t) {
        return updatedClusterList.contains(t.getClusterId());
    }

}
