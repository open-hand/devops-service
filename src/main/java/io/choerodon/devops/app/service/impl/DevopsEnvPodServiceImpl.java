package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.devops.api.vo.AgentPodInfoVO;
import io.choerodon.devops.api.vo.ContainerVO;
import io.choerodon.devops.api.vo.DevopsEnvPodInfoVO;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.app.service.AgentPodService;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.K8sUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by Zenger on 2018/4/17.
 */
@Service
public class DevopsEnvPodServiceImpl implements DevopsEnvPodService {

    private static JSON json = new JSON();
    private final Logger logger = LoggerFactory.getLogger(DevopsEnvPodServiceImpl.class);

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper;
    @Autowired
    private AgentPodService agentPodService;

    @Override
    public PageInfo<DevopsEnvPodVO> pageByOptions(Long projectId, Long envId, Long appServiceId, Long instanceId, PageRequest pageRequest, String searchParam) {
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        PageInfo<DevopsEnvPodDTO> devopsEnvPodDTOPageInfo = basePageByIds(projectId, envId, appServiceId, instanceId, pageRequest, searchParam);
        PageInfo<DevopsEnvPodVO> devopsEnvPodVOPageInfo = ConvertUtils.convertPage(devopsEnvPodDTOPageInfo, DevopsEnvPodVO.class);

        devopsEnvPodVOPageInfo.setList(devopsEnvPodDTOPageInfo.getList().stream().map(devopsEnvPodDTO -> {
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
            devopsEnvPodMapper.insert(devopsEnvPodDTO);
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
    public PageInfo<DevopsEnvPodDTO> basePageByIds(Long projectId, Long envId, Long appServiceId, Long instanceId, PageRequest pageRequest, String searchParam) {

        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("name")) {
                            property = "dp.`name`";
                        } else if (property.equals("ip")) {
                            property = "dp.ip";
                        } else if (property.equals("creationDate")) {
                            property = "dp.creation_date";
                        }

                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }
        PageInfo<DevopsEnvPodDTO> devopsEnvPodDOPage;
        if (!org.apache.commons.lang.StringUtils.isEmpty(searchParam)) {
            Map<String, Object> searchParamMap = json.deserialize(searchParam, Map.class);
            devopsEnvPodDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> devopsEnvPodMapper.listAppServicePod(
                    projectId,
                    envId,
                    appServiceId,
                    instanceId,
                    TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAMS))));
        } else {
            devopsEnvPodDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> devopsEnvPodMapper.listAppServicePod(projectId, envId, appServiceId, instanceId, null, null));
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
    public DevopsEnvPodDTO queryByNameAndEnvName(String name, String namespace) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setName(name);
        devopsEnvPodDTO.setNamespace(namespace);
        return devopsEnvPodMapper.selectOne(devopsEnvPodDTO);
    }

    @Override
    public List<DevopsEnvPodInfoVO> queryEnvPodInfo(Long envId, String sort) {
        List<DevopsEnvPodInfoVO> devopsEnvPodInfoVOList = devopsEnvPodMapper.queryEnvPodIns(envId);
        devopsEnvPodInfoVOList.forEach(devopsEnvPodInfoVO -> {
            AgentPodInfoVO agentPodInfoVO = agentPodService.queryLatestPodSnapshot(devopsEnvPodInfoVO.getName(), devopsEnvPodInfoVO.getNamespace());
            if (agentPodInfoVO != null) {
                devopsEnvPodInfoVO.setCpuUsed(agentPodInfoVO.getCpuUsed());
                devopsEnvPodInfoVO.setCpuValue(K8sUtil.getNormalValueFromCpuString(agentPodInfoVO.getCpuUsed()));
                devopsEnvPodInfoVO.setMemoryUsed(agentPodInfoVO.getMemoryUsed());
                devopsEnvPodInfoVO.setMemoryValue(K8sUtil.getByteFromMemoryString(agentPodInfoVO.getMemoryUsed()));
                devopsEnvPodInfoVO.setPodIp(agentPodInfoVO.getPodIp());
            }
        });

        // 根据cpu进行逆序排序，考虑为null值的情况
        if ("cpu".equals(sort)) {
            devopsEnvPodInfoVOList = devopsEnvPodInfoVOList.stream()
                    .sorted(Comparator.comparing(DevopsEnvPodInfoVO::getCpuValue, Comparator.nullsFirst(Double::compareTo)).reversed())
                    .collect(Collectors.toList());
        }

        // 默认根据memory进行逆序排序，考虑为null值的情况
        if ("memory".equals(sort)) {
            devopsEnvPodInfoVOList = devopsEnvPodInfoVOList.stream()
                    .sorted(Comparator.comparing(DevopsEnvPodInfoVO::getMemoryValue, Comparator.nullsFirst(Long::compareTo)).reversed())
                    .collect(Collectors.toList());
        }

        return devopsEnvPodInfoVOList;
    }
}
