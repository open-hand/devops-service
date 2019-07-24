package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsEnvPodContainerLogVO;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.app.service.DevopsEnvPodContainerService;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.util.ConvertUtils;
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
    private DevopsEnvPodService devopsEnvPodService;


    @Override
    public List<DevopsEnvPodContainerLogVO> logByPodId(Long podId) {

        DevopsEnvPodDTO devopsEnvPodDTO = devopsEnvPodService.baseQueryById(podId);
        if (devopsEnvPodDTO == null) {
            throw new CommonException("error.pod.notExist");
        }
        DevopsEnvPodVO devopsEnvPodVO = ConvertUtils.convertObject(devopsEnvPodDTO, DevopsEnvPodVO.class);
        devopsEnvPodService.setContainers(devopsEnvPodVO);
        return devopsEnvPodVO.getContainers()
                .stream()
                .map(containerDTO -> new DevopsEnvPodContainerLogVO(devopsEnvPodDTO.getName(), containerDTO.getName()))
                .collect(Collectors.toList());
    }
}
