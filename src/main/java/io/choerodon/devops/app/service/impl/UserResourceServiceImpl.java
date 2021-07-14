package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.ClusterDetailResourceVO;
import io.choerodon.devops.api.vo.GeneralResourceVO;
import io.choerodon.devops.api.vo.HostDetailResourceVO;
import io.choerodon.devops.api.vo.host.ResourceUsageInfoVO;
import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.app.service.UserResourceService;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户资源查询接口默认实现
 *
 * @author xingxingwu.hand-china.com 2021/07/13 16:56
 */
@Service
public class UserResourceServiceImpl implements UserResourceService {
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsHostMapper devopsHostMapper;
    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;

    @Autowired
    private DevopsHostService devopsHostService;

    @Override
    public GeneralResourceVO queryGeneral(Long organizationId) {
        List<ProjectDTO> projects = queryProject(organizationId);
        GeneralResourceVO result = new GeneralResourceVO();
        if (CollectionUtils.isEmpty(projects)) {
            return result;
        }
        List<Long> projectIds = projects.stream().map(ProjectDTO::getId).collect(Collectors.toList());

        /*
         *处理主机概览信息
         */
        List<DevopsHostDTO> hostDTOS = devopsHostMapper.listByProject(projectIds);
        if (CollectionUtils.isNotEmpty(hostDTOS)) {
            result.setHostTotal(hostDTOS.size());
            List<GeneralResourceVO.ResourceGroup> resourceGroups = new ArrayList<>();
            Map<Long, List<DevopsHostDTO>> projectMap = hostDTOS.stream().collect(Collectors
                    .groupingBy(DevopsHostDTO::getProjectId));
            for (Map.Entry<Long, List<DevopsHostDTO>> entry : projectMap.entrySet()) {
                ProjectDTO project = projects.stream().filter(item -> Objects.equals(entry.getKey(), item.getId()))
                        .findFirst().orElse(null);
                if (project == null) {
                    //理论是是不会为空的
                    continue;
                }
                resourceGroups.add(new GeneralResourceVO.ResourceGroup(project.getName(), entry.getValue().size(), true));
            }
            result.setHostGroups(resourceGroups);
        }

        List<DevopsClusterDTO> clusterDTOS = devopsClusterMapper.listByProject(organizationId, projectIds);
        if (CollectionUtils.isNotEmpty(clusterDTOS)) {
            result.setClusterTotal(clusterDTOS.size());
            List<GeneralResourceVO.ResourceGroup> resourceGroups = new ArrayList<>();
            Map<Long, List<DevopsClusterDTO>> projectMap = clusterDTOS.stream().collect(Collectors.groupingBy(DevopsClusterDTO::getProjectId));
            for (Map.Entry<Long, List<DevopsClusterDTO>> entry : projectMap.entrySet()) {
                ProjectDTO project = projects.stream().filter(item -> Objects.equals(entry.getKey(), item.getId()))
                        .findFirst().orElse(null);
                if (project == null) {
                    //理论是是不会为空的
                    continue;
                }
                resourceGroups.add(new GeneralResourceVO.ResourceGroup(project.getName(), entry.getValue().size(), true));
            }
            result.setClusterGroups(resourceGroups);
        }
        List<DevopsEnvironmentDTO> environmentDTOS = devopsEnvironmentMapper.listByProject(projectIds);
        if (CollectionUtils.isNotEmpty(environmentDTOS)) {
            result.setEnvTotal(environmentDTOS.size());
            List<GeneralResourceVO.ResourceGroup> resourceGroups = new ArrayList<>();
            Map<Long, List<DevopsEnvironmentDTO>> projectMap = environmentDTOS.stream().collect(Collectors.groupingBy(DevopsEnvironmentDTO::getProjectId));
            for (Map.Entry<Long, List<DevopsEnvironmentDTO>> entry : projectMap.entrySet()) {
                ProjectDTO project = projects.stream().filter(item -> Objects.equals(entry.getKey(), item.getId()))
                        .findFirst().orElse(null);
                if (project == null) {
                    //理论是是不会为空的
                    continue;
                }
                resourceGroups.add(new GeneralResourceVO.ResourceGroup(project.getName(), entry.getValue().size(), true));
            }
            result.setEnvGroups(resourceGroups);
        }
        return result;
    }

    @Override
    public List<HostDetailResourceVO> queryHostResource(Long organizationId) {
        List<HostDetailResourceVO> result = new ArrayList<>();
        List<ProjectDTO> projects = queryProject(organizationId);
        if (CollectionUtils.isEmpty(projects)) {
            return result;
        }
        List<Long> projectIds = projects.stream().map(ProjectDTO::getId).collect(Collectors.toList());
        List<DevopsHostDTO> hostDTOS = devopsHostMapper.listByProject(projectIds);
        if (CollectionUtils.isEmpty(hostDTOS)) {
            return result;
        }
        hostDTOS.forEach(hostDTO -> {
            ProjectDTO project = projects.stream().filter(projectDTO -> Objects.equals(projectDTO.getId(),
                    hostDTO.getId())).findFirst().orElse(null);
            ResourceUsageInfoVO resourceUsageInfo = devopsHostService.queryResourceUsageInfo(hostDTO.getProjectId(), hostDTO.getId());
            result.add(HostDetailResourceVO.build(hostDTO, resourceUsageInfo, project == null ? null : project.getName()));
        });
        return result;
    }

    @Override
    public List<ClusterDetailResourceVO> queryClusterResource(Long organizationId) {
        List<ClusterDetailResourceVO> result = new ArrayList<>();
        List<ProjectDTO> projects = queryProject(organizationId);
        if (CollectionUtils.isEmpty(projects)) {
            return result;
        }
        List<Long> projectIds = projects.stream().map(ProjectDTO::getId).collect(Collectors.toList());
        List<DevopsClusterDTO> clusterDTOS = devopsClusterMapper.listByProject(organizationId, projectIds);
        if (CollectionUtils.isEmpty(clusterDTOS)) {
            return result;
        }
        clusterDTOS.forEach(hostDTO -> {
            ProjectDTO project = projects.stream().filter(projectDTO -> Objects.equals(projectDTO.getId(),
                    hostDTO.getId())).findFirst().orElse(null);
            result.add(ClusterDetailResourceVO.build(hostDTO, project == null ? null : project.getName()));
        });
        return result;
    }

    private List<ProjectDTO> queryProject(Long organizationId) {
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (userDetails == null) {
            throw new CommonException(BaseConstants.ErrorCode.NOT_LOGIN);
        }
        List<ProjectDTO> projects = baseServiceClientOperator.listProjectsByUserId(organizationId,
                userDetails.getUserId());
//        ProjectDTO projectDTO = new ProjectDTO();
//        projectDTO.setId(159349538124681216L);
//        projectDTO.setName("wxx");
//        List<ProjectDTO> projects = Arrays.asList(projectDTO);

        return projects;

    }
}
