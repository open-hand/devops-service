package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.devops.domain.application.repository.DevopsEnvApplicationRepostitory;
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvApplicationMapper;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1beta2Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Service
public class DevopsEnvApplicationServiceImpl implements DevopsEnvApplicationService {

    private JSON json = new JSON();

    @Autowired
    private DevopsEnvApplicationRepostitory devopsEnvApplicationRepostitory;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DevopsEnvApplicationMapper devopsEnvApplicationMapper;

    @Autowired
    DevopsEnvApplicationMapper devopsEnvApplicationMapper;

    @Override
    public List<DevopsEnvApplicationVO> batchCreate(DevopsEnvApplicationCreationVO devopsEnvApplicationCreationVO) {
        return Stream.of(devopsEnvApplicationCreationVO.getAppIds())
                .map(appId -> new DevopsEnvApplicationDTO(devopsEnvApplicationCreationVO.getEnvId(), appId))
                .peek(e -> devopsEnvApplicationMapper.insertIgnore(ConvertHelper.convert(e, DevopsEnvApplicationDO.class)))
                .map(e -> ConvertHelper.convert(e, DevopsEnvApplicationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationRepVO> queryAppByEnvId(Long envId) {
        List<Long> appIds = devopsEnvApplicationRepostitory.baseQueryAppByEnvId(envId);
        List<ApplicationRepVO> repDTOS = new ArrayList<>();
        appIds.forEach(v ->
                repDTOS.add(ConvertHelper.convert(applicationRepository.query(v), ApplicationRepVO.class))
        );
        return repDTOS;
    }

    @Override
    public List<DevopsEnvLabelDTO> queryLabelByAppEnvId(Long envId, Long appId) {
        List<DevopsEnvMessageVO> messageES = devopsEnvApplicationRepostitory.baseListResourceByEnvAndApp(envId, appId);
        List<DevopsEnvLabelDTO> labelDTOS = new ArrayList<>();
        messageES.forEach(v -> {
            DevopsEnvLabelDTO labelDTO = new DevopsEnvLabelDTO();
            labelDTO.setResourceName(v.getResourceName());
            V1beta2Deployment v1beta2Deployment = json.deserialize(
                    v.getDetail(), V1beta2Deployment.class);
            labelDTO.setLabels(v1beta2Deployment.getSpec().getSelector().getMatchLabels());
            labelDTOS.add(labelDTO);
        });
        return labelDTOS;
    }

    @Override
    public List<DevopsEnvPortDTO> queryPortByAppEnvId(Long envId, Long appId) {
        List<DevopsEnvMessageVO> messageES = devopsEnvApplicationRepostitory.baseListResourceByEnvAndApp(envId, appId);
        List<DevopsEnvPortDTO> portDTOS = new ArrayList<>();
        messageES.forEach(v -> {
            V1beta2Deployment v1beta2Deployment = json.deserialize(
                    v.getDetail(), V1beta2Deployment.class);
            List<V1Container> containers = v1beta2Deployment.getSpec().getTemplate().getSpec().getContainers();
            for (V1Container container : containers) {
                List<V1ContainerPort> ports = container.getPorts();

                Optional.ofNullable(ports).ifPresent(portList -> {
                    for (V1ContainerPort port : portList) {
                        DevopsEnvPortDTO portDTO = new DevopsEnvPortDTO();
                        portDTO.setResourceName(v.getResourceName());
                        portDTO.setPortName(port.getName());
                        portDTO.setPortValue(port.getContainerPort());
                        portDTOS.add(portDTO);
                    }
                });
            }
        });
        return portDTOS;
    }


    @Override
    public DevopsEnvApplicationDTO baseCreate(DevopsEnvApplicationDTO devopsEnvApplicationDTO) {
        if (devopsEnvApplicationMapper.insert(devopsEnvApplicationDTO) != 1) {
            throw new CommonException("error.insert.env.app");
        }
        return devopsEnvApplicationDTO;
    }

    @Override
    public List<Long> baseQueryAppByEnvId(Long envId) {
        return devopsEnvApplicationMapper.queryAppByEnvId(envId);
    }

    @Override
    public List<DevopsEnvMessageVO> baseListResourceByEnvAndApp(Long envId, Long appId) {
        return devopsEnvApplicationMapper.listResourceByEnvAndApp(envId, appId);
    }
}
