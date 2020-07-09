package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.app.service.DevopsProjectOverview;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.agile.SprintDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.AgileServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper;
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

    private static final String ENV_UP = "up";
    private static final String ENV_DOWN = "down";

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

    @Override
    public Map<String, Long> getEnvStatusCount(Long projectId) {

        Map<String, Long> count = new HashMap<>();

        List<Long> updatedClusterList = clusterConnectionHandler.getUpdatedClusterList();

        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentMapper.listByProjectId(projectId);

        count.put(ENV_UP, devopsEnvironmentDTOS.stream()
                .filter(t -> isEnvUp(updatedClusterList, t))
                .count());

        count.put(ENV_DOWN, devopsEnvironmentDTOS.stream()
                .filter(t -> !isEnvUp(updatedClusterList, t))
                .count());

        return count;
    }

    @Override
    public Map<String, Long> getAppServiceStatusCount(Long projectId) {
        Map<String, Long> count = new HashMap<>();

        List<AppServiceDTO> appServiceDTOList = appServiceMapper.listByProjectId(projectId, null, null);

        count.put(ENV_UP, appServiceDTOList.stream()
                .filter(AppServiceDTO::getActive)
                .count());

        count.put(ENV_DOWN, appServiceDTOList.stream()
                .filter(t -> !t.getActive())
                .count());

        return count;
    }

    @Override
    public Map<String, Long> getCommitCount(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        SprintDTO sprintDTO = agileServiceClientOperator.getActiveSprint(projectId, projectDTO.getOrganizationId());
        List<Date> dateList = devopsGitlabCommitMapper.queryCountByProjectIdAndCreationDate(projectId, sprintDTO.getStartDate());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateList.stream().map(simpleDateFormat::format)
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
    }

    private boolean isEnvUp(List<Long> updatedClusterList, DevopsEnvironmentDTO t) {
        return updatedClusterList.contains(t.getClusterId());
    }

}
