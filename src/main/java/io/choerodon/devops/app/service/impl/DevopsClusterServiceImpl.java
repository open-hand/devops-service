package io.choerodon.devops.app.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsClusterRepDTO;
import io.choerodon.devops.api.dto.DevopsClusterReqDTO;
import io.choerodon.devops.api.dto.ProjectDTO;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.domain.application.entity.DevopsClusterE;
import io.choerodon.devops.domain.application.entity.DevopsClusterProPermissionE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.DevopsClusterProPermissionRepository;
import io.choerodon.devops.domain.application.repository.DevopsClusterRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.GenerateUUID;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;
import io.choerodon.devops.infra.feign.IamServiceClient;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsClusterServiceImpl implements DevopsClusterService {

    @Value("${agent.version}")
    private String agentExpectVersion;

    @Value("${agent.serviceUrl}")
    private String agentServiceUrl;

    @Value("${agent.repoUrl}")
    private String agentRepoUrl;

    @Autowired
    private DevopsClusterRepository devopsClusterRepository;
    @Autowired
    private DevopsClusterProPermissionRepository devopsClusterProPermissionRepository;
    @Autowired
    private IamServiceClient iamServiceClient;
    @Autowired
    private IamRepository iamRepository;

    @Override
    @Transactional
    public String createCluster(Long organizationId, DevopsClusterReqDTO devopsClusterReqDTO) {
        DevopsClusterE devopsClusterE = new DevopsClusterE();
        BeanUtils.copyProperties(devopsClusterReqDTO, devopsClusterE);
        devopsClusterE.setToken(GenerateUUID.generateUUID());
        devopsClusterE.setOrganizationId(organizationId);
        devopsClusterE = devopsClusterRepository.create(devopsClusterE);
        if (!devopsClusterE.getSkipCheckProjectPermission()) {
            for (Long projectId : devopsClusterReqDTO.getProjects()) {
                DevopsClusterProPermissionE devopsClusterProPermissionE = new DevopsClusterProPermissionE();
                devopsClusterProPermissionE.setClusterId(devopsClusterE.getId());
                devopsClusterProPermissionE.setProjectId(projectId);
                ProjectE projectE = iamRepository.queryIamProject(projectId);
                devopsClusterProPermissionE.setProjectName(projectE.getName());
                devopsClusterProPermissionRepository.insert(devopsClusterProPermissionE);
            }
        }
        InputStream inputStream = this.getClass().getResourceAsStream("/shell/cluster.sh");
        Map<String, String> params = new HashMap<>();
        params.put("{VERSION}", agentExpectVersion);
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsClusterE.getToken());
        params.put("{REPOURL}", agentRepoUrl);
        params.put("{CLUSTERID}", devopsClusterE
                .getId().toString());
        return FileUtil.replaceReturnString(inputStream, params);
    }

    @Override
    @Transactional
    public void updateCluster(Long clusterId, DevopsClusterReqDTO devopsClusterReqDTO) {
        List<Long> projects = devopsClusterReqDTO.getProjects();
        Boolean skipCheckPro = devopsClusterReqDTO.getSkipCheckProjectPermission();
        List<Long> addProjects = new ArrayList<>();
        DevopsClusterE devopsClusterE = devopsClusterRepository.query(clusterId);
        //以前不跳过项目权限校验,但是现在跳过，情况集群对应的项目集群校验表
        if (skipCheckPro && !devopsClusterE.getSkipCheckProjectPermission()) {
            devopsClusterProPermissionRepository.deleteByClusterId(clusterId);
        } else {
            //操作集群项目权限校验表记录
            List<Long> projectIds = devopsClusterProPermissionRepository.listByClusterId(clusterId).stream().map(DevopsClusterProPermissionE::getProjectId).collect(Collectors.toList());
            projects.forEach(projectId -> {
                if (!projectIds.contains(projectId)) {
                    addProjects.add(projectId);
                } else {
                    projectIds.remove(projectId);
                }
            });
            addProjects.forEach(addProject -> {
                DevopsClusterProPermissionE devopsClusterProPermissionE = new DevopsClusterProPermissionE();
                devopsClusterProPermissionE.setClusterId(clusterId);
                devopsClusterProPermissionE.setProjectId(addProject);
                ProjectE projectE = iamRepository.queryIamProject(addProject);
                devopsClusterProPermissionE.setProjectName(projectE.getName());
                devopsClusterProPermissionRepository.insert(devopsClusterProPermissionE);
            });
            projectIds.forEach(deleteProject -> {
                DevopsClusterProPermissionE devopsClusterProPermissionE = new DevopsClusterProPermissionE();
                devopsClusterProPermissionE.setClusterId(clusterId);
                devopsClusterProPermissionE.setProjectId(deleteProject);
                devopsClusterProPermissionRepository.delete(devopsClusterProPermissionE);
            });
        }
        devopsClusterE = ConvertHelper.convert(devopsClusterReqDTO, DevopsClusterE.class);
        devopsClusterE.setId(clusterId);
        devopsClusterRepository.update(devopsClusterE);
    }

    @Override
    public void checkName(Long organizationId, String name) {
        DevopsClusterE devopsClusterE = new DevopsClusterE();
        devopsClusterE.setOrganizationId(organizationId);
        devopsClusterE.setName(name);
        devopsClusterRepository.checkName(devopsClusterE);
    }

    @Override
    public Page<ProjectDTO> listProjects(Long organizationId, Long clusterId, PageRequest pageRequest, String[] params) {
        Page<ProjectDO> projects = iamServiceClient.queryProjectByOrgId(organizationId, pageRequest.getPage(), pageRequest.getSize(), null, params).getBody();
        Page<ProjectDTO> pageProjectDTOS = new Page<>();
        List<ProjectDTO> projectDTOS = new ArrayList<>();
        if (projects.getContent() != null) {
            BeanUtils.copyProperties(projects, pageProjectDTOS);
            List<Long> projectIds;
            if (clusterId != null) {
                projectIds = devopsClusterProPermissionRepository.listByClusterId(clusterId).stream().map(DevopsClusterProPermissionE::getProjectId).collect(Collectors.toList());
            } else {
                projectIds = new ArrayList<>();
            }
            projects.getContent().forEach(projectDO -> {
                ProjectDTO projectDTO = new ProjectDTO();
                projectDTO.setId(projectDO.getId());
                projectDTO.setName(projectDO.getName());
                if (projectIds.contains(projectDO.getId())) {
                    projectDTO.setPermission(true);
                }
                projectDTOS.add(projectDTO);
            });
        }
        pageProjectDTOS.setContent(projectDTOS);
        return pageProjectDTOS;
    }

    @Override
    public String queryShell(Long clusterId) {
        DevopsClusterE devopsClusterE = devopsClusterRepository.query(clusterId);
        InputStream inputStream = this.getClass().getResourceAsStream("/shell/cluster.sh");
        Map<String, String> params = new HashMap<>();
        params.put("{VERSION}", agentExpectVersion);
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsClusterE.getToken());
        params.put("{REPOURL}", agentRepoUrl);
        params.put("{CLUSTERID}", devopsClusterE
                .getId().toString());
        return FileUtil.replaceReturnString(inputStream, params);
    }

    @Override
    public void checkCode(Long organizationId, String code) {
        DevopsClusterE devopsClusterE = new DevopsClusterE();
        devopsClusterE.setOrganizationId(organizationId);
        devopsClusterE.setCode(code);
        devopsClusterRepository.checkName(devopsClusterE);
    }

    @Override
    public Page<DevopsClusterRepDTO> pageClusters(Long organizationId, PageRequest pageRequest) {
        return ConvertPageHelper.convertPage(PageHelper.doPageAndSort(pageRequest, () -> devopsClusterRepository.pageClusters(organizationId, pageRequest)), DevopsClusterRepDTO.class);
    }

    @Override
    public List<ProjectDTO> listClusterProjects(Long organizationId, Long clusterId) {
        return devopsClusterProPermissionRepository.listByClusterId(clusterId).stream().map(devopsClusterProPermissionE -> {
            ProjectDTO projectDTO = new ProjectDTO();
            projectDTO.setId(devopsClusterProPermissionE.getProjectId());
            projectDTO.setName(devopsClusterProPermissionE.getProjectName());
            return projectDTO;
        }).collect(Collectors.toList());
    }


}
