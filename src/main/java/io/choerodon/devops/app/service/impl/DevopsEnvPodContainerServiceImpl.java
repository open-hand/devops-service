package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsEnvPodContainerLogDTO;
import io.choerodon.devops.app.service.DevopsEnvPodContainerService;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creator: Runge
 * Date: 2018/5/16
 * Time: 13:55
 * Description:
 */
@Component
public class DevopsEnvPodContainerServiceImpl implements DevopsEnvPodContainerService {

    @Autowired
    private DevopsEnvPodRepository podRepository;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;


    @Override
    public List<DevopsEnvPodContainerLogDTO> logByPodId(Long podId) {

        DevopsEnvPodE devopsEnvPodE = podRepository.get(podId);
        if (devopsEnvPodE == null) {
            throw new CommonException("error.pod.notExist");
        }
        devopsEnvPodService.setContainers(devopsEnvPodE);
        List<DevopsEnvPodContainerLogDTO> devopsEnvPodContainerLogDTOS = devopsEnvPodE.getContainers().stream().map(containerDTO -> {
            DevopsEnvPodContainerLogDTO devopsEnvPodContainerLogDTO = new DevopsEnvPodContainerLogDTO(devopsEnvPodE.getName(), containerDTO.getName());
            return devopsEnvPodContainerLogDTO;
        }).collect(Collectors.toList());

        return devopsEnvPodContainerLogDTOS;
    }
}
