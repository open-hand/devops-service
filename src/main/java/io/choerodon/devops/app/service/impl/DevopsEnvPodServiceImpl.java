package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.devops.api.vo.ContainerDTO;
import io.choerodon.devops.api.vo.DevopsEnvironmentPodVO;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvPodE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.infra.util.ArrayUtil;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.util.K8sUtil;
import io.choerodon.devops.infra.enums.ResourceType;
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
    private final Logger logger = LoggerFactory.getLogger(DevopsEnvPodServiceImpl.class);

    private final ClusterConnectionHandler clusterConnectionHandler;
    private final DevopsEnvPodRepository devopsEnvPodRepository;
    private final DevopsEnvironmentRepository devopsEnvironmentRepository;
    private final DevopsEnvResourceRepository devopsEnvResourceRepository;

    @Autowired
    public DevopsEnvPodServiceImpl(ClusterConnectionHandler clusterConnectionHandler, DevopsEnvPodRepository devopsEnvPodRepository, DevopsEnvironmentRepository devopsEnvironmentRepository, DevopsEnvResourceRepository devopsEnvResourceRepository) {
        this.clusterConnectionHandler = clusterConnectionHandler;
        this.devopsEnvPodRepository = devopsEnvPodRepository;
        this.devopsEnvironmentRepository = devopsEnvironmentRepository;
        this.devopsEnvResourceRepository = devopsEnvResourceRepository;
    }


    @Override
    public PageInfo<DevopsEnvironmentPodVO> listAppPod(Long projectId, Long envId, Long appId, Long instanceId, PageRequest pageRequest, String searchParam) {
        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        PageInfo<DevopsEnvPodE> devopsEnvPodEPage = devopsEnvPodRepository.listAppPod(projectId, envId, appId, instanceId, pageRequest, searchParam);
        devopsEnvPodEPage.getList().forEach(devopsEnvPodE -> {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsEnvPodE.getEnvId());
            devopsEnvPodE.setClusterId(devopsEnvironmentE.getClusterE().getId());
            if (connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId())) {
                devopsEnvPodE.setConnect(true);
            }

            setContainers(devopsEnvPodE);
        });

        return ConvertPageHelper.convertPageInfo(devopsEnvPodEPage, DevopsEnvironmentPodVO.class);
    }

    /**
     * set the containers of the pod
     *
     * @param devopsEnvPodE the pod entity
     */
    @Override
    public void setContainers(DevopsEnvPodE devopsEnvPodE) {
        String message = devopsEnvResourceRepository.getResourceDetailByNameAndTypeAndInstanceId(devopsEnvPodE.getApplicationInstanceE().getId(), devopsEnvPodE.getName(), ResourceType.POD);

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
            devopsEnvPodE.setContainers(result);
        } catch (Exception e) {
            logger.info("名为 '{}' 的Pod的资源解析失败", devopsEnvPodE.getName());
        }
    }
}
