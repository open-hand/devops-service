package io.choerodon.devops.app.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.CountVO;
import io.choerodon.devops.api.vo.MergeRequestVO;
import io.choerodon.devops.api.vo.iam.ImmutableProjectInfoVO;
import io.choerodon.devops.app.service.AppServiceInstanceService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.app.service.DevopsMergeRequestService;
import io.choerodon.devops.app.service.DevopsProjectOverview;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.agile.SprintDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.AgileServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.DevopsCiCdPipelineMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsProjectOverviewImpl implements DevopsProjectOverview {

    private static final String UP = "up";
    private static final String DOWN = "down";
    // 创建 DateTimeFormatter 对象
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    private AgileServiceClientOperator agileServiceClientOperator;

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;

    @Autowired
    private DevopsMergeRequestService devopsMergeRequestService;

    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;

    @Autowired
    private AppServiceMapper appServiceMapper;

    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper;

    @Autowired
    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

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
    public CountVO getCommitCount(Long projectId) {
        ImmutableProjectInfoVO projectDTO = baseServiceClientOperator.queryImmutableProjectInfo(projectId);
        SprintDTO sprintDTO = agileServiceClientOperator.getActiveSprint(projectId, projectDTO.getTenantId());
        if (sprintDTO == null || sprintDTO.getSprintId() == null) {
            return new CountVO();
        }
        List<Date> dateList = devopsGitlabCommitMapper.queryCountByProjectIdAndDate(projectId, new java.sql.Date(sprintDTO.getStartDate().getTime()), new java.sql.Date(sprintDTO.getEndDate().getTime()));

        Map<String, Long> dateCount = dateList.stream().map(date -> {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            return dateTimeFormatter.format(localDateTime);
        })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        List<String> date = dateCount.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());

        List<Long> count = new ArrayList<>();

        date.forEach(d -> count.add(dateCount.get(d)));


        CountVO result = new CountVO();

        result.setDate(date);
        result.setCount(count);

        return result;
    }

    @Override
    public CountVO getDeployCount(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

        // 该项目下所有环境
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentMapper.listByProjectId(projectId);

        List<Long> envIds = devopsEnvironmentDTOS.stream()
                .map(DevopsEnvironmentDTO::getId)
                .collect(Collectors.toList());

        if (envIds.isEmpty()) {
            return new CountVO();
        }

        SprintDTO sprintDTO = agileServiceClientOperator.getActiveSprint(projectId, projectDTO.getOrganizationId());
        if (sprintDTO == null || sprintDTO.getSprintId() == null) {
            return new CountVO();
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


        List<Long> count = new ArrayList<>();
        creationDates.forEach(date -> {
            Long[] newDeployFrequency = {0L};
            resultMaps.get(date).forEach(deployFrequencyDO -> newDeployFrequency[0] = newDeployFrequency[0] + 1L);
            count.add(newDeployFrequency[0]);
        });

        CountVO result = new CountVO();

        result.setDate(creationDates);
        result.setCount(count);
        return result;
    }

    @Override
    public CountVO getCiCount(Long projectId) {
        ImmutableProjectInfoVO projectDTO = baseServiceClientOperator.queryImmutableProjectInfo(projectId);
        SprintDTO sprintDTO = agileServiceClientOperator.getActiveSprint(projectId, projectDTO.getTenantId());
        if (sprintDTO == null || sprintDTO.getSprintId() == null) {
            return new CountVO();
        }
        //根据项目的id查询项目下所有的流水线的id
        List<CiCdPipelineDTO> ciCdPipelineDTOS = devopsCiCdPipelineMapper.selectPipelineByProjectId(projectId);
        if (CollectionUtils.isEmpty(ciCdPipelineDTOS)) {
            return new CountVO();
        }
        //查询流水线在这个冲刺中的部署次数
        List<DevopsCiPipelineRecordDTO> devopsPipelineRecordRelDTOS = new ArrayList<>();
        ciCdPipelineDTOS.forEach(ciCdPipelineDTO ->
                //当前冲刺下流水线的触发次数
                devopsPipelineRecordRelDTOS.addAll(devopsCiPipelineRecordService.listByPipelineId(ciCdPipelineDTO.getId(),
                        new java.sql.Date(sprintDTO.getStartDate().getTime()),
                        new java.sql.Date(sprintDTO.getEndDate().getTime()))));
        if (CollectionUtils.isEmpty(devopsPipelineRecordRelDTOS)) {
            return new CountVO();
        }
        Map<String, Long> dateCount = devopsPipelineRecordRelDTOS.stream().map(DevopsCiPipelineRecordDTO::getCreationDate)
                .map(date -> {
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                    return dateTimeFormatter.format(localDateTime);
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));


        List<String> date = dateCount.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        List<Long> count = new ArrayList<>();
        date.forEach(d -> count.add(dateCount.get(d)));
        CountVO result = new CountVO();
        result.setDate(date);
        result.setCount(count);
        return result;
    }

    @Override
    public Page<MergeRequestVO> getMergeRequestToBeChecked(Long projectId, Set<Long> appServiceIdsToSearch, String param, PageRequest pageRequest) {
        return devopsMergeRequestService.getMergeRequestToBeChecked(projectId, appServiceIdsToSearch, param, pageRequest);
    }

    private boolean isEnvUp(List<Long> updatedClusterList, DevopsEnvironmentDTO t) {
        return updatedClusterList.contains(t.getClusterId());
    }

}
