package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsEnvPodContainerDTO;
import io.choerodon.devops.api.dto.DevopsEnvPodContainerLogDTO;
import io.choerodon.devops.app.service.DevopsEnvPodContainerService;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodContainerRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodContainerDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/5/16
 * Time: 13:55
 * Description:
 */
@Component
public class DevopsEnvPodContainerServiceImpl implements DevopsEnvPodContainerService {
    @Autowired
    private DevopsEnvPodContainerRepository containerRepository;
    @Autowired
    private DevopsEnvPodRepository podRepository;

    @Override
    public DevopsEnvPodContainerLogDTO log(Long containerId) {
        DevopsEnvPodContainerDO container = containerRepository.get(containerId);
        if (container == null) {
            throw new CommonException("error.container.notExist");
        }
        DevopsEnvPodE pod = podRepository.get(container.getPodId());
        if (pod == null) {
            throw new CommonException("error.pod.notExist");
        }
        return new DevopsEnvPodContainerLogDTO(pod.getName(),
                container.getContainerName());
    }

    @Override
    public DevopsEnvPodContainerLogDTO logByPodId(Long podId) {
        DevopsEnvPodContainerDO container = containerRepository.get(new DevopsEnvPodContainerDO(podId));
        if (container == null) {
            throw new CommonException("error.container.notExist");
        }
        DevopsEnvPodE pod = podRepository.get(podId);
        if (pod == null) {
            throw new CommonException("error.pod.notExist");
        }
        return new DevopsEnvPodContainerLogDTO(pod.getName(),
                container.getContainerName());
    }

    @Override
    public Page<DevopsEnvPodContainerDTO> listByOptions(Long podId, PageRequest pageRequest, String searchParam) {
        return ConvertPageHelper.convertPage(
                containerRepository.page(podId, pageRequest, searchParam),
                DevopsEnvPodContainerDTO.class);
    }
}
