package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.devops.api.vo.ContainerDTO;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsEnvPodMapper;
import io.choerodon.devops.infra.util.ArrayUtil;
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
    private final ClusterConnectionHandler clusterConnectionHandler;
    private final DevopsEnvPodRepository devopsEnvPodRepository;
    private final DevopsEnvironmentRepository devopsEnvironmentRepository;
    private final DevopsEnvResourceRepository devopsEnvResourceRepository;
    @Autowired
    private DevopsEnvPodMapper devopsEnvPodMapper;

    @Autowired
    public DevopsEnvPodServiceImpl(ClusterConnectionHandler clusterConnectionHandler, DevopsEnvPodRepository devopsEnvPodRepository, DevopsEnvironmentRepository devopsEnvironmentRepository, DevopsEnvResourceRepository devopsEnvResourceRepository) {
        this.clusterConnectionHandler = clusterConnectionHandler;
        this.devopsEnvPodRepository = devopsEnvPodRepository;
        this.devopsEnvironmentRepository = devopsEnvironmentRepository;
        this.devopsEnvResourceRepository = devopsEnvResourceRepository;
    }


    @Override
    public PageInfo<DevopsEnvPodVO> listAppPod(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam) {
        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        PageInfo<DevopsEnvPodE> devopsEnvPodEPage = devopsEnvPodRepository.basePageByIds(projectId, envId, appId, instanceId, pageRequest, searchParam);
        devopsEnvPodEPage.getList().forEach(devopsEnvPodE -> {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.baseQueryById(devopsEnvPodE.getEnvId());
            devopsEnvPodE.setClusterId(devopsEnvironmentE.getClusterE().getId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                devopsEnvPodE.setConnect(true);
            }

            setContainers(devopsEnvPodE);
        });

        return ConvertPageHelper.convertPageInfo(devopsEnvPodEPage, DevopsEnvPodVO.class);
    }

    @Override
    public void setContainers(DevopsEnvPodDTO devopsEnvPodDTO) {
        String message = devopsEnvResourceRepository.getResourceDetailByNameAndTypeAndInstanceId(devopsEnvPodDTO.getAppInstanceId(), devopsEnvPodDTO.getName(), ResourceType.POD);

        if (StringUtils.isEmpty(message)) {
            return;
        }

        try {
            V1Pod pod = K8sUtil.deserialize(message, V1Pod.class);
            List<ContainerDTO> containers = pod.getStatus().getContainerStatuses()
                    .stream()
                    .map(container -> {
                        ContainerDTO containerDTO = new ContainerDTO();
                        containerDTO.setName(container.getName());
                        containerDTO.setReady(container.isReady());
                        return containerDTO;
                    })
                    .collect(Collectors.toList());

            // 将不可用的容器置于靠前位置
            Map<Boolean, List<ContainerDTO>> containsByStatus = containers.stream().collect(Collectors.groupingBy(container -> container.getReady() == null ? Boolean.FALSE : container.getReady()));
            List<ContainerDTO> result = new ArrayList<>();
            if (!ArrayUtil.isEmpty(containsByStatus.get(Boolean.FALSE))) {
                result.addAll(containsByStatus.get(Boolean.FALSE));
            }
            if (!ArrayUtil.isEmpty(containsByStatus.get(Boolean.TRUE))) {
                result.addAll(containsByStatus.get(Boolean.TRUE));
            }
            devopsEnvPodDTO.setContainers(result);
        } catch (Exception e) {
            logger.info("名为 '{}' 的Pod的资源解析失败", devopsEnvPodDTO.getName());
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
        DevopsEnvPodDTO devopsEnvPodDO = new DevopsEnvPodDTO();
        devopsEnvPodDO.setAppInstanceId(instanceId);
        return devopsEnvPodMapper.select(devopsEnvPodDO);
    }

    @Override
    public PageInfo<DevopsEnvPodDTO> basePageByIds(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam) {

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
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> devopsEnvPodMapper.listAppPod(
                    projectId,
                    envId,
                    appId,
                    instanceId,
                    TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                    TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
        } else {
            devopsEnvPodDOPage = PageHelper.startPage(
                    pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> devopsEnvPodMapper.listAppPod(projectId, envId, appId, instanceId, null, null));
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
}
