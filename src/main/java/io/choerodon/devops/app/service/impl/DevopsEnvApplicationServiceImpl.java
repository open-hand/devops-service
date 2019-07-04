package io.choerodon.devops.app.service.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.ApplicationRepDTO;
import io.choerodon.devops.api.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.api.dto.DevopsEnvLabelDTO;
import io.choerodon.devops.api.dto.DevopsEnvPortDTO;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.devops.domain.application.entity.DevopsEnvApplicationE;
import io.choerodon.devops.domain.application.entity.DevopsEnvMessageE;
import io.choerodon.devops.domain.application.repository.ApplicationInstanceRepository;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvApplicationRepostitory;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1beta2Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Service
public class DevopsEnvApplicationServiceImpl implements DevopsEnvApplicationService {

    private static JSON json = new JSON();

    @Autowired
    DevopsEnvApplicationRepostitory devopsEnvApplicationRepostitory;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    ApplicationInstanceRepository applicationInstanceRepository;

    @Override
    public DevopsEnvApplicationDTO create(DevopsEnvApplicationDTO devopsEnvApplicationDTO) {
        return ConvertHelper.convert(devopsEnvApplicationRepostitory.create(
                ConvertHelper.convert(devopsEnvApplicationDTO, DevopsEnvApplicationE.class)), DevopsEnvApplicationDTO.class);
    }

    @Override
    public List<ApplicationRepDTO> queryAppByEnvId(Long envId) {
        List<Long> appIds = devopsEnvApplicationRepostitory.queryAppByEnvId(envId);
        List<ApplicationRepDTO> repDTOS = new ArrayList<>();
        appIds.forEach(v ->
                repDTOS.add(ConvertHelper.convert(applicationRepository.query(v), ApplicationRepDTO.class))
        );
        return repDTOS;
    }

    @Override
    public List<DevopsEnvLabelDTO> queryLabelByAppEnvId(Long envId, Long appId) {
        List<DevopsEnvMessageE> messageES = devopsEnvApplicationRepostitory.listResourceByEnvAndApp(envId, appId);
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
        List<DevopsEnvMessageE> messageES = devopsEnvApplicationRepostitory.listResourceByEnvAndApp(envId, appId);
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
}
