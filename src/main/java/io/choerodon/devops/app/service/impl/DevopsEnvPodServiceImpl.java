package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ContainerVO;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;

/**
 * Created by Zenger on 2018/4/17.
 */
@Service
public class DevopsEnvPodServiceImpl implements DevopsEnvPodService {

    private static JSON json = new JSON();
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsEnvPodServiceImpl.class);

    private static final String ERROR_DELETE_POD_FAILED = "devops.delete.pod.failed";

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper;
    @Autowired
    private AgentPodService agentPodService;
    @Autowired
    @Lazy
    private DevopsClusterService devopsClusterService;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private DevopsEnvUserPermissionService devopsEnvUserPermissionService;
    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    @Override
    public Page<DevopsEnvPodVO> pageByOptions(Long projectId, Long envId, Long appServiceId, Long instanceId, PageRequest pageable, String searchParam) {
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();
        Page<DevopsEnvPodDTO> devopsEnvPodDTOPageInfo = basePageByIds(projectId, envId, appServiceId, instanceId, pageable, searchParam);
        Page<DevopsEnvPodVO> devopsEnvPodVOPageInfo = ConvertUtils.convertPage(devopsEnvPodDTOPageInfo, DevopsEnvPodVO.class);

        devopsEnvPodVOPageInfo.setContent(devopsEnvPodDTOPageInfo.getContent().stream().map(devopsEnvPodDTO -> {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsEnvPodDTO.getEnvId());
            DevopsEnvPodVO devopsEnvPodVO = ConvertUtils.convertObject(devopsEnvPodDTO, DevopsEnvPodVO.class);
            devopsEnvPodVO.setClusterId(devopsEnvironmentDTO.getClusterId());
            devopsEnvPodVO.setConnect(updatedEnvList.contains(devopsEnvironmentDTO.getClusterId()));
            //给pod设置containers
            fillContainers(devopsEnvPodVO);
            return devopsEnvPodVO;
        }).collect(Collectors.toList()));

        return devopsEnvPodVOPageInfo;
    }

    @Override
    public void fillContainers(DevopsEnvPodVO devopsEnvPodVO) {

        //解析pod的yaml内容获取container的信息
        String message = devopsEnvResourceService.getResourceDetailByNameAndTypeAndInstanceId(devopsEnvPodVO.getInstanceId(), devopsEnvPodVO.getName(), ResourceType.POD);

        if (StringUtils.isEmpty(message)) {
            return;
        }

        try {
            V1Pod pod = K8sUtil.deserialize(message, V1Pod.class);
            if (pod.getStatus() != null && !ObjectUtils.isEmpty(pod.getStatus().getContainerStatuses())) {
                List<ContainerVO> containers = pod.getStatus().getContainerStatuses().stream().map(container -> {
                    ContainerVO containerVO = new ContainerVO();
                    containerVO.setName(container.getName());
                    containerVO.setReady(container.getReady());
                    return containerVO;
                }).collect(Collectors.toList());

                // 将不可用的容器置于靠前位置
                Map<Boolean, List<ContainerVO>> containsByStatus = containers.stream().collect(Collectors.groupingBy(container -> container.getReady() == null ? Boolean.FALSE : container.getReady()));
                List<ContainerVO> result = new ArrayList<>();
                if (!ArrayUtil.isEmpty(containsByStatus.get(Boolean.FALSE))) {
                    result.addAll(containsByStatus.get(Boolean.FALSE));
                }
                if (!ArrayUtil.isEmpty(containsByStatus.get(Boolean.TRUE))) {
                    result.addAll(containsByStatus.get(Boolean.TRUE));
                }
                devopsEnvPodVO.setContainers(result);
            } else {
                LOGGER.info("pod status or container status is null :{}", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("名为 '{}' 的Pod的资源解析失败", devopsEnvPodVO.getName());
        }
    }


    @Override
    public DevopsEnvPodDTO baseQueryById(Long id) {
        return devopsEnvPodMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsEnvPodDTO baseQueryByEnvIdAndName(Long envId, String name) {
        Assert.notNull(envId, ResourceCheckConstant.DEVOPS_ENV_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.DEVOPS_POD_NAME_IS_NULL);

        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setEnvId(envId);
        devopsEnvPodDTO.setName(name);
        return devopsEnvPodMapper.selectOne(devopsEnvPodDTO);
    }

    @Override
    public void baseCreate(DevopsEnvPodDTO devopsEnvPodDTO) {
        DevopsEnvPodDTO envPodDTO = new DevopsEnvPodDTO();
        envPodDTO.setName(devopsEnvPodDTO.getName());
        envPodDTO.setNamespace(devopsEnvPodDTO.getNamespace());
        if (devopsEnvPodMapper.selectOne(envPodDTO) == null) {
            MapperUtil.resultJudgedInsert(devopsEnvPodMapper, devopsEnvPodDTO, "devops.insert.env.pod");
        }
    }

    @Override
    public void baseUpdate(DevopsEnvPodDTO devopsEnvPodDTO) {
        devopsEnvPodMapper.updateByPrimaryKey(devopsEnvPodDTO);
    }

    @Override
    public List<DevopsEnvPodDTO> baseListByInstanceId(Long instanceId) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setInstanceId(instanceId);
        return devopsEnvPodMapper.select(devopsEnvPodDTO);
    }

    @Override
    public Page<DevopsEnvPodDTO> basePageByIds(Long projectId, Long envId, Long appServiceId, Long instanceId, PageRequest pageable, String searchParam) {
        Sort sort = pageable.getSort();
        if (sort != null) {
            List<Sort.Order> newOrder = new ArrayList<>();
            sort.iterator().forEachRemaining(s -> {
                String property = s.getProperty();
                if ("name".equals(property)) {
                    property = "dp.`name`";
                } else if ("ip".equals(property)) {
                    property = "dp.ip";
                } else if ("creationDate".equals(property)) {
                    property = "dp.creation_date";
                }
                newOrder.add(new Sort.Order(s.getDirection(), property));
            });
            pageable.setSort(new Sort(newOrder));
        }
        Page<DevopsEnvPodDTO> devopsEnvPodDOPage;
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            devopsEnvPodDOPage = PageHelper.doPageAndSort(pageable, () -> devopsEnvPodMapper.listAppServicePod(projectId, envId, appServiceId, instanceId, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))));
        } else {
            devopsEnvPodDOPage = PageHelper.doPageAndSort(pageable, () -> devopsEnvPodMapper.listAppServicePod(projectId, envId, appServiceId, instanceId, null, null));
        }

        return devopsEnvPodDOPage;
    }

    @Override
    public void baseDeleteByName(String name, String namespace) {
        DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO();
        devopsEnvPodDO.setName(name);
        devopsEnvPodDO.setNamespace(namespace);
        List<DevopsEnvPodDTO> devopsEnvPodDOs = devopsEnvPodMapper.select(devopsEnvPodDO);
        if (!devopsEnvPodDOs.isEmpty()) {
            devopsEnvPodMapper.delete(devopsEnvPodDOs.get(0));
        }
    }

    @Override
    @Transactional
    public void baseDeleteByNameAndEnvId(String name, Long envId) {
        Assert.notNull(name, ResourceCheckConstant.DEVOPS_POD_NAME_IS_NULL);
        Assert.notNull(envId, ResourceCheckConstant.DEVOPS_ENV_ID_IS_NULL);

        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setEnvId(envId);
        devopsEnvPodDTO.setName(name);
        if (devopsEnvPodMapper.delete(devopsEnvPodDTO) != 1) {
            throw new CommonException(ERROR_DELETE_POD_FAILED);
        }

    }

    @Override
    public void baseDeleteById(Long id) {
        devopsEnvPodMapper.deleteByPrimaryKey(id);
    }

    private static Map<String, DevopsEnvResourceDTO> listToMap(List<DevopsEnvResourceDTO> resources) {
        Map<String, DevopsEnvResourceDTO> map = new HashMap<>();
        for (DevopsEnvResourceDTO resource : resources) {
            if (map.get(resource.getName()) == null) {
                map.put(resource.getName(), resource);
            } else {
                map.put(resource.getName(), compareRevision(map.get(resource.getName()), resource));
            }
        }
        return map;
    }

    private static DevopsEnvResourceDTO compareRevision(DevopsEnvResourceDTO one, DevopsEnvResourceDTO theOther) {
        if (one == null || one.getReversion() == null) {
            return theOther;
        }
        if (theOther == null || theOther.getReversion() == null) {
            return one;
        }
        return one.getReversion() > theOther.getReversion() ? one : theOther;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEnvPodById(Long projectId, Long envId, Long podId) {
        DevopsEnvPodDTO devopsEnvPodDTO = baseQueryById(podId);
        // 查询不到pod直接返回
        if (devopsEnvPodDTO == null) {
            return;
        }
        //检验环境相关信息
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        agentCommandService.deletePod(devopsEnvPodDTO.getName(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getClusterId());
    }

    @Override
    public List<PodResourceDetailsDTO> queryResourceDetailsByInstanceId(Long instanceId) {
        return devopsEnvPodMapper.queryResourceDetailsByInstanceId(instanceId);
    }

    @Override
    public boolean checkLogAndExecPermission(Long projectId, Long clusterId, String envCode, Long userId, String podName) {

        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        // 先检查是不是查看agent的pod日志
        if (envCode.equals(devopsClusterDTO.getNamespace()) && podName.equals(devopsClusterDTO.getPodName())) {
            return true;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryByClusterIdAndCode(clusterId, envCode);
        if (devopsEnvironmentDTO == null) {
            LOGGER.info("The env with clusterId {} and envCode {} doesn't exist", clusterId, envCode);
            return false;
        }

        if (!devopsEnvironmentDTO.getProjectId().equals(projectId)) {
            LOGGER.info("The provided project id {} doesn't equal to env's {}", projectId, devopsEnvironmentDTO.getProjectId());
            return false;
        }

        Long envId = devopsEnvironmentDTO.getId();
        // 校验用户有环境的权限
        if (!devopsEnvUserPermissionService.userFromWebsocketHasPermission(userId, devopsEnvironmentDTO)) {
            LOGGER.info("User {} is not permitted to the env with id {}", userId, envId);
            return false;
        }

        return true;
    }

    @Override
    public Page<DevopsEnvPodVO> pageByKind(Long projectId, Long envId, String kind, String name, PageRequest pageable, String searchParam) {
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();

        DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService.baseQueryOptions(null, null, envId, kind, name);

        if (devopsEnvResourceDTO != null && devopsEnvResourceDTO.getInstanceId() != null) {
            AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsEnvResourceDTO.getInstanceId());
            return pageByOptions(projectId, envId, appServiceInstanceDTO.getAppServiceId(), devopsEnvResourceDTO.getInstanceId(), pageable, searchParam);
        }

        Page<DevopsEnvPodDTO> devopsEnvPodDTOPageInfo = basePageByKind(envId, kind, name, pageable, searchParam);
        Page<DevopsEnvPodVO> devopsEnvPodVOPageInfo = ConvertUtils.convertPage(devopsEnvPodDTOPageInfo, DevopsEnvPodVO.class);

        devopsEnvPodVOPageInfo.setContent(devopsEnvPodDTOPageInfo.getContent().stream().map(devopsEnvPodDTO -> {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsEnvPodDTO.getEnvId());
            DevopsEnvPodVO devopsEnvPodVO = ConvertUtils.convertObject(devopsEnvPodDTO, DevopsEnvPodVO.class);
            devopsEnvPodVO.setClusterId(devopsEnvironmentDTO.getClusterId());
            devopsEnvPodVO.setConnect(updatedEnvList.contains(devopsEnvironmentDTO.getClusterId()));
            //给pod设置containers
            fillContainers(envId, devopsEnvPodVO);
            return devopsEnvPodVO;
        }).collect(Collectors.toList()));

        return devopsEnvPodVOPageInfo;
    }

    @Override
    public List<DevopsEnvPodDTO> listPodByKind(Long envId, String kind, String name) {
        return devopsEnvPodMapper.listPodByKind(envId, kind, name, null, null);
    }

    @Override
    public boolean checkInstancePodStatusAllReadyWithCommandId(Long envId, Long appId, Long commandId) {

        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.selectByPrimaryKey(appId);

        if (devopsDeployAppCenterEnvDTO == null) {
            LOGGER.info(">>>>>>[checkPodStatus]envId: {}, appid: {},app not found, is deleted?<<<<<<<<<", envId, appId);
            return false;
        }
        // 查询实例
        AppServiceInstanceDTO instanceE = appServiceInstanceService.baseQuery(devopsDeployAppCenterEnvDTO.getObjectId());

        if (instanceE == null) {
            LOGGER.info(">>>>>>[checkPodStatus]envId: {}, appid: {},instance not found, is deleted?<<<<<<<<<", envId, appId);
            return false;
        }
        String instanceCode = instanceE.getCode();
        // 查询部署版本

        // 查询当前实例运行时pod metadata
        List<PodResourceDetailsDTO> podResourceDetailsDTOS = queryResourceDetailsByInstanceId(instanceE.getId());

        if (CollectionUtils.isEmpty(podResourceDetailsDTOS)) {
            LOGGER.info(">>>>>>[checkPodStatus]envId: {}, instanceCode: {},podResourceDetailsDTOS is empty<<<<<<<<<", envId, instanceCode);
            return false;
        }
        for (PodResourceDetailsDTO podResourceDetailsDTO : podResourceDetailsDTOS) {
            V1Pod podInfo = K8sUtil.deserialize(podResourceDetailsDTO.getMessage(), V1Pod.class);

            // 校验是否所有pod都启动成功
            if (Boolean.FALSE.equals(podResourceDetailsDTO.getReady())) {
                LOGGER.info(">>>>>>>[checkPodStatus]envId: {}, instanceCode: {},pod：{} not ready<<<<<<<<<", envId, instanceCode, podInfo.getMetadata().getName());
                return false;
            }

            // commandId不为null,则还要判断pod的command标签
            if (commandId != null) {
                String podCommandId = podInfo.getMetadata().getLabels().get(KubernetesConstants.CHOERODON_IO_V1_COMMAND);
                if (podCommandId == null) {
                    LOGGER.info(">>>>>>>>[checkPodStatus]envId: {}, instanceCode: {},pod:{} Command not Found <<<<<<<<<", envId, instanceCode, podInfo.getMetadata().getName());
                    return false;
                } else if (Long.parseLong(podCommandId) < commandId) {
                    LOGGER.info(">>>>>>>>[checkPodStatus]envId: {}, instanceCode: {},pod:{} Command before require commandId <<<<<<<", envId, instanceCode, podInfo.getMetadata().getName());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<DevopsEnvPodVO> listWorkloadPod(String ownerKind, String ownerName) {
        return devopsEnvPodMapper.listWorkloadPod(ownerKind, ownerName);
    }

    private void fillContainers(Long envId, DevopsEnvPodVO devopsEnvPodVO) {
        //解析pod的yaml内容获取container的信息

        String message = devopsEnvResourceService.getResourceDetailByEnvIdAndKindAndName(envId, devopsEnvPodVO.getName(), ResourceType.POD);

        if (StringUtils.isEmpty(message)) {
            return;
        }

        try {
            V1Pod pod = K8sUtil.deserialize(message, V1Pod.class);
            List<ContainerVO> containers = pod.getStatus().getContainerStatuses().stream().map(container -> {
                ContainerVO containerVO = new ContainerVO();
                containerVO.setName(container.getName());
                containerVO.setReady(container.getReady());
                return containerVO;
            }).collect(Collectors.toList());

            // 将不可用的容器置于靠前位置
            Map<Boolean, List<ContainerVO>> containsByStatus = containers.stream().collect(Collectors.groupingBy(container -> container.getReady() == null ? Boolean.FALSE : container.getReady()));
            List<ContainerVO> result = new ArrayList<>();
            if (!ArrayUtil.isEmpty(containsByStatus.get(Boolean.FALSE))) {
                result.addAll(containsByStatus.get(Boolean.FALSE));
            }
            if (!ArrayUtil.isEmpty(containsByStatus.get(Boolean.TRUE))) {
                result.addAll(containsByStatus.get(Boolean.TRUE));
            }
            devopsEnvPodVO.setIp(pod.getStatus().getPodIP());
            devopsEnvPodVO.setContainers(result);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("名为 '{}' 的Pod的资源解析失败", devopsEnvPodVO.getName());
        }

    }

    private Page<DevopsEnvPodDTO> basePageByKind(Long envId, String kind, String name, PageRequest pageable, String searchParam) {
        Sort sort = pageable.getSort();
        if (sort != null) {
            List<Sort.Order> newOrder = new ArrayList<>();
            sort.iterator().forEachRemaining(s -> {
                String property = s.getProperty();
                if ("name".equals(property)) {
                    property = "dp.`name`";
                } else if ("ip".equals(property)) {
                    property = "dp.ip";
                } else if ("creationDate".equals(property)) {
                    property = "dp.creation_date";
                }
                newOrder.add(new Sort.Order(s.getDirection(), property));
            });
            pageable.setSort(new Sort(newOrder));
        }
        Page<DevopsEnvPodDTO> devopsEnvPodDOPage;
        if (!ObjectUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            devopsEnvPodDOPage = PageHelper.doPageAndSort(pageable, () -> devopsEnvPodMapper.listPodByKind(envId, kind, name, TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)), TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))));
        } else {
            devopsEnvPodDOPage = PageHelper.doPageAndSort(pageable, () -> devopsEnvPodMapper.listPodByKind(envId, kind, name, null, null));
        }

        return devopsEnvPodDOPage;
    }
}
