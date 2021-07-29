package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ContainerVO;
import io.choerodon.devops.api.vo.DevopsEnvPodInfoVO;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.api.vo.PodMetricsRedisInfoVO;
import io.choerodon.devops.app.service.*;
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
    private final Logger logger = LoggerFactory.getLogger(DevopsEnvPodServiceImpl.class);

    private static final String ERROR_DELETE_POD_FAILED = "error.delete.pod.failed";

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
            List<ContainerVO> containers = pod.getStatus().getContainerStatuses()
                    .stream()
                    .map(container -> {
                        ContainerVO containerVO = new ContainerVO();
                        containerVO.setName(container.getName());
                        containerVO.setReady(container.isReady());
                        return containerVO;
                    })
                    .collect(Collectors.toList());

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
        } catch (Exception e) {
            logger.info("名为 '{}' 的Pod的资源解析失败", devopsEnvPodVO.getName());
        }
    }


    @Override
    public DevopsEnvPodDTO baseQueryById(Long id) {
        return devopsEnvPodMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsEnvPodDTO baseQueryByEnvIdAndName(Long envId, String name) {
        Assert.notNull(envId, ResourceCheckConstant.ERROR_ENV_ID_IS_NULL);
        Assert.notNull(name, ResourceCheckConstant.ERROR_POD_NAME_IS_NULL);

        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setEnvId(envId);
        devopsEnvPodDTO.setName(name);
        return devopsEnvPodMapper.selectOne(devopsEnvPodDTO);
    }

    @Override
    public DevopsEnvPodDTO baseQueryByPod(DevopsEnvPodDTO devopsEnvPodDTO) {
        List<DevopsEnvPodDTO> devopsEnvPodDOS =
                devopsEnvPodMapper.select(devopsEnvPodDTO);
        if (devopsEnvPodDOS.isEmpty()) {
            return null;
        }
        return devopsEnvPodDOS.get(0);
    }

    @Override
    public void baseCreate(DevopsEnvPodDTO devopsEnvPodDTO) {
        DevopsEnvPodDTO envPodDTO = new DevopsEnvPodDTO();
        envPodDTO.setName(devopsEnvPodDTO.getName());
        envPodDTO.setNamespace(devopsEnvPodDTO.getNamespace());
        if (devopsEnvPodMapper.selectOne(envPodDTO) == null) {
            MapperUtil.resultJudgedInsert(devopsEnvPodMapper, devopsEnvPodDTO, "error.insert.env.pod");
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
            devopsEnvPodDOPage = PageHelper.doPageAndSort(pageable, () -> devopsEnvPodMapper.listAppServicePod(
                    projectId,
                    envId,
                    appServiceId,
                    instanceId,
                    TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))));
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
        Assert.notNull(name, ResourceCheckConstant.ERROR_POD_NAME_IS_NULL);
        Assert.notNull(envId, ResourceCheckConstant.ERROR_ENV_ID_IS_NULL);

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

    @Override
    public DevopsEnvPodDTO queryByNameAndEnvName(String name, String namespace) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setName(name);
        devopsEnvPodDTO.setNamespace(namespace);
        return devopsEnvPodMapper.selectOne(devopsEnvPodDTO);
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
    public List<DevopsEnvPodInfoVO> queryEnvPodInfo(Long envId, String sort) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(devopsEnvironmentDTO.getClusterId());
        List<DevopsEnvPodInfoVO> devopsEnvPodInfoVOList = devopsEnvPodMapper.queryEnvPodIns(envId);

        // 根据devopsEnvPodInfoVOList获取name集合，批量查询devopsEnvResourceDTO和DevopsEnvResourceDetailDTO
        List<String> podNames = devopsEnvPodInfoVOList.stream().map(DevopsEnvPodInfoVO::getName).collect(Collectors.toList());
        List<DevopsEnvResourceDTO> devopsEnvResourceDTOList = devopsEnvResourceService.listEnvResourceByOptions(envId, ResourceType.POD.getType(), podNames);
        Set<Long> resourceDetailIds = devopsEnvResourceDTOList.stream().map(DevopsEnvResourceDTO::getResourceDetailId).collect(Collectors.toSet());
        Map<String, DevopsEnvResourceDTO> devopsEnvResourceMap = listToMap(devopsEnvResourceDTOList);
        List<DevopsEnvResourceDetailDTO> devopsEnvResourceDetailDTOS = devopsEnvResourceDetailService.listByMessageIds(resourceDetailIds);
        Map<Long, DevopsEnvResourceDetailDTO> devopsEnvResourceDetailMap = devopsEnvResourceDetailDTOS.stream().collect(Collectors.toMap(DevopsEnvResourceDetailDTO::getId, Function.identity()));

        devopsEnvPodInfoVOList.forEach(devopsEnvPodInfoVO -> {
            PodMetricsRedisInfoVO podMetricsRedisInfoVO = agentPodService.queryLatestPodSnapshot(devopsEnvPodInfoVO.getName(), devopsEnvPodInfoVO.getNamespace(), devopsClusterDTO.getCode());
            DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceMap.get(devopsEnvPodInfoVO.getName());
            DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = devopsEnvResourceDetailMap.get(devopsEnvResourceDTO.getResourceDetailId());
            V1Pod v1Pod = json.deserialize(devopsEnvResourceDetailDTO.getMessage(), V1Pod.class);
            devopsEnvPodInfoVO.setStatus(K8sUtil.changePodStatus(v1Pod));
            devopsEnvPodInfoVO.setPodIp(v1Pod == null ? null : v1Pod.getStatus().getPodIP());
            if (podMetricsRedisInfoVO != null) {
                devopsEnvPodInfoVO.setCpuUsed(podMetricsRedisInfoVO.getCpu());
                devopsEnvPodInfoVO.setMemoryUsed(podMetricsRedisInfoVO.getMemory());
            }
        });

        // 根据cpu进行逆序排序，考虑为null值的情况
        if ("cpu".equals(sort)) {
            devopsEnvPodInfoVOList = devopsEnvPodInfoVOList.stream()
                    .sorted(Comparator.comparing(DevopsEnvPodInfoVO::getCpuUsed, Comparator.nullsFirst(String::compareTo)).reversed())
                    .collect(Collectors.toList());
        }

        // 默认根据memory进行逆序排序，考虑为null值的情况
        if ("memory".equals(sort)) {
            devopsEnvPodInfoVOList = devopsEnvPodInfoVOList.stream()
                    .sorted(Comparator.comparing(DevopsEnvPodInfoVO::getMemoryUsed, Comparator.nullsFirst(String::compareTo)).reversed())
                    .collect(Collectors.toList());
        }

        return devopsEnvPodInfoVOList;
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
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryByClusterIdAndCode(clusterId, envCode);
        if (devopsEnvironmentDTO == null) {
            logger.info("The env with clusterId {} and envCode {} doesn't exist", clusterId, envCode);
            return false;
        }

        if (!devopsEnvironmentDTO.getProjectId().equals(projectId)) {
            logger.info("The provided project id {} doesn't equal to env's {}", projectId, devopsEnvironmentDTO.getProjectId());
            return false;
        }

        Long envId = devopsEnvironmentDTO.getId();
        // 校验用户有环境的权限
        if (!devopsEnvUserPermissionService.userFromWebsocketHasPermission(userId, devopsEnvironmentDTO)) {
            logger.info("User {} is not permitted to the env with id {}", userId, envId);
            return false;
        }

        // 校验pod存在 自动化测试的查看日志没有pod，但是也要查看日志
//        if (!podExists(envId, podName)) {
//            logger.info("The pod with name {} doesn't exist in the env with id {}", podName, envId);
//            return false;
//        }

        return true;
    }

    @Override
    public Page<DevopsEnvPodVO> pageByKind(Long projectId, Long envId, String kind, String name, PageRequest pageable, String searchParam) {
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();

        DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService.baseQueryOptions(null, null, envId, kind, name);

        if (devopsEnvResourceDTO.getInstanceId() != null) {
            AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsEnvResourceDTO.getInstanceId());
            return pageByOptions(projectId,
                    envId,
                    appServiceInstanceDTO.getAppServiceId(),
                    devopsEnvResourceDTO.getInstanceId(),
                    pageable,
                    searchParam);
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

    private void fillContainers(Long envId, DevopsEnvPodVO devopsEnvPodVO) {
        //解析pod的yaml内容获取container的信息

        String message = devopsEnvResourceService.getResourceDetailByEnvIdAndKindAndName(envId, devopsEnvPodVO.getName(), ResourceType.POD);

        if (StringUtils.isEmpty(message)) {
            return;
        }

        try {
            V1Pod pod = K8sUtil.deserialize(message, V1Pod.class);
            List<ContainerVO> containers = pod.getStatus().getContainerStatuses()
                    .stream()
                    .map(container -> {
                        ContainerVO containerVO = new ContainerVO();
                        containerVO.setName(container.getName());
                        containerVO.setReady(container.isReady());
                        return containerVO;
                    })
                    .collect(Collectors.toList());

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
            logger.info("名为 '{}' 的Pod的资源解析失败", devopsEnvPodVO.getName());
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
        if (!StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            devopsEnvPodDOPage = PageHelper.doPageAndSort(pageable, () -> devopsEnvPodMapper.listPodByKind(
                    envId,
                    kind,
                    name,
                    TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))));
        } else {
            devopsEnvPodDOPage = PageHelper.doPageAndSort(pageable, () -> devopsEnvPodMapper.listPodByKind(envId, kind, name, null, null));
        }

        return devopsEnvPodDOPage;
    }

    private boolean podExists(Long envId, String podName) {
        DevopsEnvPodDTO condition = new DevopsEnvPodDTO();
        condition.setEnvId(envId);
        condition.setName(podName);
        return devopsEnvPodMapper.selectCount(condition) > 0;
    }
}
