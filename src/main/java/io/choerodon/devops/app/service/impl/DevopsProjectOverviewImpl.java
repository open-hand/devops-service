package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsProjectOverview;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DeployDTO;
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
import java.util.*;
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
    private AppServiceInstanceService appServiceInstanceService;

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
        if (sprintDTO.getSprintId() == null) {
            return new HashMap<>();
        }
        List<Date> dateList = devopsGitlabCommitMapper.queryCountByProjectIdAndDate(projectId, new java.sql.Date(sprintDTO.getStartDate().getTime()), new java.sql.Date(sprintDTO.getEndDate().getTime()));

        return dateList.stream().map(simpleDateFormat::format)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
    }

    @Override
    public Map<String, Long> getDeployCount(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

        // 该项目下所有启用环境
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentMapper.listByProjectId(projectId);

        List<Long> envIds = devopsEnvironmentDTOS.stream()
                .map(DevopsEnvironmentDTO::getId)
                .collect(Collectors.toList());

        if (envIds.size() == 0) {
            return new HashMap<>();
        }

        SprintDTO sprintDTO = agileServiceClientOperator.getActiveSprint(projectId, projectDTO.getOrganizationId());
        if (sprintDTO.getSprintId() == null) {
            return new HashMap<>();
        }

        List<DeployDTO> deployDTOS = appServiceInstanceService.baseListDeployFrequency(projectId, envIds.toArray(new Long[0]), null, sprintDTO.getStartDate(), sprintDTO.getEndDate());

        //以时间维度分组
        Map<String, List<DeployDTO>> resultMaps = deployDTOS.stream()
                .collect(Collectors.groupingBy(t -> new java.sql.Date(t.getCreationDate().getTime()).toString()));

        List<String> creationDates = deployDTOS.stream()
                .map(deployDTO -> new java.sql.Date(deployDTO.getCreationDate().getTime()).toString())
                .collect(Collectors.toList());
        creationDates = new ArrayList<>(new HashSet<>(creationDates)).stream().sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());


        Map<String, Long> count = new HashMap<>();
        creationDates.forEach(date -> {
            Long[] newDeployFrequency = {0L};
            resultMaps.get(date).forEach(deployFrequencyDO -> newDeployFrequency[0] = newDeployFrequency[0] + 1L);
            count.put(date, newDeployFrequency[0]);
        });
        return count;
    }

    private boolean isEnvUp(List<Long> updatedClusterList, DevopsEnvironmentDTO t) {
        return updatedClusterList.contains(t.getClusterId());
    }

}
