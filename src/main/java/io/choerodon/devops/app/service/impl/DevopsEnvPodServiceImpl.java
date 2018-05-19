package io.choerodon.devops.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.DevopsEnvPodDTO;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodRepository;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/17.
 */
@Service
public class DevopsEnvPodServiceImpl implements DevopsEnvPodService {

    private DevopsEnvPodRepository devopsEnvPodRepository;

    public DevopsEnvPodServiceImpl(DevopsEnvPodRepository devopsEnvPodRepository) {
        this.devopsEnvPodRepository = devopsEnvPodRepository;
    }

    @Override
    public Page<DevopsEnvPodDTO> listAppPod(Long projectId, PageRequest pageRequest, String searchParam) {
        return ConvertPageHelper.convertPage(
                devopsEnvPodRepository.listAppPod(projectId, pageRequest, searchParam), DevopsEnvPodDTO.class);
    }
}
