package io.choerodon.devops.app.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;
import io.choerodon.devops.infra.util.*;


@Service
public class DevopsClusterServiceImpl implements DevopsClusterService {

    @Value("${agent.version}")
    private String agentExpectVersion;

    @Value("${agent.serviceUrl}")
    private String agentServiceUrl;

    @Value("${agent.repoUrl}")
    private String agentRepoUrl;

    private JSON json = new JSON();

    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private ClusterNodeInfoService clusterNodeInfoService;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private DevopsClusterProPermissionService devopsClusterProPermissionService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;


    @Override
    @Transactional
    public String createCluster(Long organizationId, DevopsClusterReqVO devopsClusterReqVO) {

        // 插入记录
        DevopsClusterDTO devopsClusterDTO = ConvertUtils.convertObject(devopsClusterReqVO, DevopsClusterDTO.class);
        devopsClusterDTO.setToken(GenerateUUID.generateUUID());
        devopsClusterDTO.setOrganizationId(organizationId);
        devopsClusterDTO = baseCreateCluster(devopsClusterDTO);

        //插入集群项目关系表
        if (!devopsClusterDTO.getSkipCheckProjectPermission() && devopsClusterReqVO.getProjects() != null) {
            for (Long projectId : devopsClusterReqVO.getProjects()) {
                DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
                devopsClusterProPermissionDTO.setClusterId(devopsClusterDTO.getId());
                devopsClusterProPermissionDTO.setProjectId(projectId);
                devopsClusterProPermissionService.baseInsertPermission(devopsClusterProPermissionDTO);
            }
        }

        IamUserDTO iamUserDTO = iamServiceClientOperator.queryUserByUserId(GitUserNameUtil.getUserId().longValue());

        // 渲染激活环境的命令参数
        InputStream inputStream = this.getClass().getResourceAsStream("/shell/cluster.sh");
        Map<String, String> params = new HashMap<>();
        params.put("{VERSION}", agentExpectVersion);
        params.put("{NAME}", "choerodon-cluster-agent-" + devopsClusterDTO.getCode());
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsClusterDTO.getToken());
        params.put("{EMAIL}", iamUserDTO == null ? "" : iamUserDTO.getEmail());
        params.put("{CHOERODONID}", devopsClusterDTO.getChoerodonId());
        params.put("{REPOURL}", agentRepoUrl);
        params.put("{CLUSTERID}", devopsClusterDTO
                .getId().toString());
        return FileUtil.replaceReturnString(inputStream, params);
    }

    @Override
    @Transactional
    public void updateCluster(Long clusterId, DevopsClusterReqVO devopsClusterReqVO) {
        List<Long> projects = devopsClusterReqVO.getProjects();
        Boolean skipCheckPro = devopsClusterReqVO.getSkipCheckProjectPermission();
        List<Long> addProjects = new ArrayList<>();
        DevopsClusterDTO devopsClusterDTO = baseQuery(clusterId);

        //以前不跳过项目权限校验,但是现在跳过，清空集群对应的项目集群校验表
        if (skipCheckPro && !devopsClusterDTO.getSkipCheckProjectPermission()) {
            devopsClusterProPermissionService.baseDeleteByClusterId(clusterId);
        } else {
            //操作集群项目权限校验表记录
            List<Long> projectIds = devopsClusterProPermissionService.baseListByClusterId(clusterId)
                    .stream().map(DevopsClusterProPermissionDTO::getProjectId).collect(Collectors.toList());

            projects.forEach(projectId -> {
                if (!projectIds.contains(projectId)) {
                    addProjects.add(projectId);
                } else {
                    projectIds.remove(projectId);
                }
            });
            addProjects.forEach(addProject -> {
                DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
                devopsClusterProPermissionDTO.setClusterId(clusterId);
                devopsClusterProPermissionDTO.setProjectId(addProject);
                devopsClusterProPermissionService.baseInsertPermission(devopsClusterProPermissionDTO);
            });
            projectIds.forEach(deleteProject -> {
                DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
                devopsClusterProPermissionDTO.setClusterId(clusterId);
                devopsClusterProPermissionDTO.setProjectId(deleteProject);
                devopsClusterProPermissionService.baseDeletePermission(devopsClusterProPermissionDTO);
            });
        }
        devopsClusterDTO = ConvertUtils.convertObject(devopsClusterReqVO, DevopsClusterDTO.class);
        devopsClusterDTO.setId(clusterId);
        baseUpdate(devopsClusterDTO);
    }

    @Override
    public void checkName(Long organizationId, String name) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setOrganizationId(organizationId);
        devopsClusterDTO.setName(name);
        baseCheckName(devopsClusterDTO);
    }

    @Override
    public PageInfo<ProjectReqVO> pageProjects(Long organizationId, Long clusterId, PageRequest pageRequest,
                                               String[] params) {
        PageInfo<ProjectDTO> projectDTOPageInfo = iamServiceClientOperator
                .pageProjectByOrgId(organizationId, pageRequest.getPage(), pageRequest.getSize(), null, params);
        PageInfo<ProjectReqVO> projectReqVOPageInfo = ConvertUtils.convertPage(projectDTOPageInfo, ProjectReqVO.class);
        List<ProjectReqVO> projectDTOS = new ArrayList<>();
        if (!projectDTOPageInfo.getList().isEmpty()) {
            List<Long> projectIds;
            if (clusterId != null) {
                projectIds = devopsClusterProPermissionService.baseListByClusterId(clusterId).stream()
                        .map(DevopsClusterProPermissionDTO::getProjectId).collect(Collectors.toList());
            } else {
                projectIds = new ArrayList<>();
            }
            projectDTOPageInfo.getList().forEach(projectDO -> {
                ProjectReqVO projectDTO = new ProjectReqVO(projectDO.getId(), projectDO.getName(), projectDO.getCode(), projectIds.contains(projectDO.getId()));
                projectDTOS.add(projectDTO);
            });
        }
        projectReqVOPageInfo.setList(projectDTOS);
        return projectReqVOPageInfo;
    }

    @Override
    public String queryShell(Long clusterId) {
        DevopsClusterRepVO devopsClusterRepVO = getDevopsClusterStatus(clusterId);
        InputStream inputStream;
        if (devopsClusterRepVO.getUpgrade()) {
            inputStream = this.getClass().getResourceAsStream("/shell/cluster-upgrade.sh");
        } else {
            inputStream = this.getClass().getResourceAsStream("/shell/cluster.sh");
        }

        //初始化渲染脚本
        IamUserDTO iamUserDTO = iamServiceClientOperator.queryUserByUserId(devopsClusterRepVO.getCreateBy());
        Map<String, String> params = new HashMap<>();
        params.put("{VERSION}", agentExpectVersion);
        params.put("{NAME}", "choerodon-cluster-agent-" + devopsClusterRepVO.getCode());
        params.put("{SERVICEURL}", agentServiceUrl);
        params.put("{TOKEN}", devopsClusterRepVO.getToken());
        params.put("{EMAIL}", iamUserDTO == null ? "" : iamUserDTO.getEmail());
        params.put("{REPOURL}", agentRepoUrl);
        params.put("{CHOERODONID}", devopsClusterRepVO.getChoerodonId());
        params.put("{CLUSTERID}", devopsClusterRepVO
                .getId().toString());
        return FileUtil.replaceReturnString(inputStream, params);
    }

    @Override
    public void checkCode(Long organizationId, String code) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setOrganizationId(organizationId);
        devopsClusterDTO.setCode(code);
        baseCheckCode(devopsClusterDTO);
    }

    @Override
    public PageInfo<ClusterWithNodesVO> pageClusters(Long organizationId, Boolean doPage, PageRequest pageRequest, String params) {
        PageInfo<DevopsClusterRepVO> devopsClusterRepVOPageInfo = ConvertUtils.convertPage(basePageClustersByOptions(organizationId, doPage, pageRequest, params), DevopsClusterRepVO.class);
        PageInfo<ClusterWithNodesVO> devopsClusterRepDTOPage = ConvertUtils.convertPage(devopsClusterRepVOPageInfo, ClusterWithNodesVO.class);

        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        devopsClusterRepVOPageInfo.getList().forEach(devopsClusterRepVO ->
                setClusterStatus(connectedEnvList, updatedEnvList, devopsClusterRepVO));

        devopsClusterRepDTOPage.setList(fromClusterE2ClusterWithNodesDTO(devopsClusterRepVOPageInfo.getList(), organizationId));
        return devopsClusterRepDTOPage;
    }

    @Override
    public List<ProjectReqVO> listClusterProjects(Long organizationId, Long clusterId) {
        return devopsClusterProPermissionService.baseListByClusterId(clusterId).stream()
                .map(devopsClusterProPermissionDTO -> {
                    ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(devopsClusterProPermissionDTO.getProjectId());
                    return new ProjectReqVO(devopsClusterProPermissionDTO.getProjectId(), projectDTO.getName(), projectDTO.getCode(), null);
                }).collect(Collectors.toList());
    }

    private void setClusterStatus(List<Long> connectedEnvList, List<Long> updatedEnvList, DevopsClusterRepVO devopsClusterRepVO) {
        if (connectedEnvList.contains(devopsClusterRepVO.getId())) {
            if (updatedEnvList.contains(devopsClusterRepVO.getId())) {
                devopsClusterRepVO.setUpgrade(false);
                devopsClusterRepVO.setConnect(true);
            } else {
                devopsClusterRepVO.setUpgrade(true);
                devopsClusterRepVO.setConnect(false);
                devopsClusterRepVO.setUpgradeMessage("Version is too low, please upgrade!");
            }
        } else {
            devopsClusterRepVO.setUpgrade(false);
            devopsClusterRepVO.setConnect(false);
        }
    }


    @Override
    public void deleteCluster(Long clusterId) {
        baseDelete(clusterId);
    }

    @Override
    public Boolean checkConnectEnvs(Long clusterId) {
        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<DevopsEnvironmentDTO> devopsEnvironmentDTOS = devopsEnvironmentService.baseListByClusterId(clusterId);
        if (connectedEnvList.contains(clusterId) || !devopsEnvironmentDTOS.isEmpty()) {
            throw new CommonException("error.cluster.delete");
        }
        return true;
    }


    @Override
    public DevopsClusterRepVO query(Long clusterId) {
        return ConvertUtils.convertObject(baseQuery(clusterId), DevopsClusterRepVO.class);
    }

    @Override
    public PageInfo<DevopsClusterPodVO> pagePodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam) {
        PageInfo<DevopsEnvPodDTO> devopsEnvPodDTOPageInfo = basePageQueryPodsByNodeName(clusterId, nodeName, pageRequest, searchParam);
        PageInfo<DevopsClusterPodVO> clusterPodDTOPage = ConvertUtils.convertPage(devopsEnvPodDTOPageInfo, DevopsClusterPodVO.class);

        clusterPodDTOPage.setList(devopsEnvPodDTOPageInfo.getList().stream().map(this::podE2ClusterPodDTO).collect(Collectors.toList()));
        return clusterPodDTOPage;
    }

    @Override
    public DevopsClusterRepVO queryByCode(Long organizationId, String code) {
        return ConvertUtils.convertObject(baseQueryByCode(organizationId, code), DevopsClusterRepVO.class);
    }


    @Override
    public DevopsClusterDTO baseCreateCluster(DevopsClusterDTO devopsClusterDTO) {
        List<DevopsClusterDTO> devopsClusterDTOS = devopsClusterMapper.selectAll();
        String choerodonId = GenerateUUID.generateUUID().split("-")[0];
        if (!devopsClusterDTOS.isEmpty()) {
            devopsClusterDTO.setChoerodonId(devopsClusterDTOS.get(0).getChoerodonId());
        } else {
            devopsClusterDTO.setChoerodonId(choerodonId);
        }
        if (devopsClusterMapper.insert(devopsClusterDTO) != 1) {
            throw new CommonException("error.devops.cluster.insert");
        }
        return devopsClusterDTO;
    }

    @Override
    public void baseCheckName(DevopsClusterDTO devopsClusterDTO) {
        if (devopsClusterMapper.selectOne(devopsClusterDTO) != null) {
            throw new CommonException("error.cluster.name.exist");
        }
    }

    @Override
    public void baseCheckCode(DevopsClusterDTO devopsClusterDTO) {
        if (devopsClusterMapper.selectOne(devopsClusterDTO) != null) {
            throw new CommonException("error.cluster.code.exist");
        }
    }

    @Override
    public List<DevopsClusterDTO> baseListByProjectId(Long projectId, Long organizationId) {
        return devopsClusterMapper.listByProjectId(projectId, organizationId);
    }

    @Override
    public DevopsClusterDTO baseQuery(Long clusterId) {
        return devopsClusterMapper.selectByPrimaryKey(clusterId);
    }

    @Override
    public void baseUpdate(DevopsClusterDTO inputClusterDTO) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(inputClusterDTO.getId());
        inputClusterDTO.setObjectVersionNumber(devopsClusterDTO.getObjectVersionNumber());
        devopsClusterMapper.updateByPrimaryKeySelective(inputClusterDTO);
        devopsClusterMapper.updateSkipCheckPro(inputClusterDTO.getId(), inputClusterDTO.getSkipCheckProjectPermission());
    }

    @Override
    public PageInfo<DevopsClusterDTO> basePageClustersByOptions(Long organizationId, Boolean doPage, PageRequest pageRequest, String params) {
        PageInfo<DevopsClusterDTO> devopsClusterEPage;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> searchParamMap = json.deserialize(params, Map.class);
            devopsClusterEPage = PageHelper
                    .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsClusterMapper.listClusters(organizationId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))));
        } else {
            devopsClusterEPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsClusterMapper.listClusters(organizationId, null, null));
        }
        return devopsClusterEPage;
    }

    @Override
    public void baseDelete(Long clusterId) {
        devopsClusterMapper.deleteByPrimaryKey(clusterId);
    }

    @Override
    public DevopsClusterDTO baseQueryByToken(String token) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setToken(token);
        return devopsClusterMapper.selectOne(devopsClusterDTO);
    }

    @Override
    public List<DevopsClusterDTO> baseList() {
        return devopsClusterMapper.selectAll();
    }

    @Override
    public PageInfo<DevopsEnvPodDTO> basePageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam) {
        return PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> devopsClusterMapper.pageQueryPodsByNodeName(clusterId, nodeName, searchParam));
    }

    @Override
    public DevopsClusterDTO baseQueryByCode(Long organizationId, String code) {
        DevopsClusterDTO devopsClusterDTO = new DevopsClusterDTO();
        devopsClusterDTO.setOrganizationId(organizationId);
        devopsClusterDTO.setCode(code);
        return devopsClusterMapper.selectOne(devopsClusterDTO);
    }

    @Override
    public void baseUpdateProjectId(Long orgId, Long proId) {
        devopsClusterMapper.updateProjectId(orgId, proId);
    }

    /**
     * pod entity to cluster pod vo
     *
     * @param devopsEnvPodDTO pod entity
     * @return the cluster pod vo
     */
    private DevopsClusterPodVO podE2ClusterPodDTO(DevopsEnvPodDTO devopsEnvPodDTO) {
        DevopsClusterPodVO devopsClusterPodVO = new DevopsClusterPodVO();
        BeanUtils.copyProperties(devopsEnvPodDTO, devopsClusterPodVO);
        DevopsEnvPodVO devopsEnvPodVO = ConvertUtils.convertObject(devopsEnvPodDTO, DevopsEnvPodVO.class);
        devopsEnvPodService.setContainers(devopsEnvPodVO);

        devopsClusterPodVO.setContainersForLogs(
                devopsEnvPodDTO.getContainers()
                        .stream()
                        .map(container -> new DevopsEnvPodContainerLogVO(devopsEnvPodDTO.getName(), container.getName()))
                        .collect(Collectors.toList())
        );
        return devopsClusterPodVO;
    }

    /**
     * convert cluster entity to instances of {@link ClusterWithNodesVO}
     *
     * @param devopsClusterRepVOS the cluster entities
     * @param organizationId      the organization id
     * @return the instances of the return type
     */
    private List<ClusterWithNodesVO> fromClusterE2ClusterWithNodesDTO(List<DevopsClusterRepVO> devopsClusterRepVOS, Long organizationId) {
        // default three records of nodes in the instance
        PageRequest pageRequest = new PageRequest(1, 3);

        return devopsClusterRepVOS.stream().map(cluster -> {
            ClusterWithNodesVO clusterWithNodesDTO = new ClusterWithNodesVO();
            BeanUtils.copyProperties(cluster, clusterWithNodesDTO);
            if (Boolean.TRUE.equals(clusterWithNodesDTO.getConnect())) {
                clusterWithNodesDTO.setNodes(clusterNodeInfoService.pageClusterNodeInfo(cluster.getId(), organizationId, pageRequest));
            }
            return clusterWithNodesDTO;
        }).collect(Collectors.toList());
    }

    private DevopsClusterRepVO getDevopsClusterStatus(Long clusterId) {
        DevopsClusterRepVO devopsClusterRepVO = ConvertUtils.convertObject(baseQuery(clusterId), DevopsClusterRepVO.class);
        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        setClusterStatus(connectedEnvList, updatedEnvList, devopsClusterRepVO);
        return devopsClusterRepVO;
    }


}
