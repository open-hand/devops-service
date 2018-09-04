package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.dto.DevopsEnvGroupDTO;
import io.choerodon.devops.app.service.DevopsEnvGroupService;
import io.choerodon.devops.domain.application.entity.DevopsEnvGroupE;
import io.choerodon.devops.domain.application.repository.DevopsEnvGroupRepository;

@Service
public class DevopsEnvGroupServiceImpl implements DevopsEnvGroupService {

    @Autowired
    private DevopsEnvGroupRepository devopsEnvGroupRepository;

    @Override
    public DevopsEnvGroupDTO create(DevopsEnvGroupDTO devopsEnvGroupDTO, Long projectId) {
        DevopsEnvGroupE devopsEnvGroupE = ConvertHelper.convert(devopsEnvGroupDTO, DevopsEnvGroupE.class);
        devopsEnvGroupE.initProject(projectId);
        devopsEnvGroupRepository.create(devopsEnvGroupE);
        return ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupDTO.class);
    }

    @Override
    public DevopsEnvGroupDTO update(DevopsEnvGroupDTO devopsEnvGroupDTO, Long projectId) {
        DevopsEnvGroupE devopsEnvGroupE = ConvertHelper.convert(devopsEnvGroupDTO, DevopsEnvGroupE.class);
        devopsEnvGroupE.initProject(projectId);
        devopsEnvGroupRepository.update(devopsEnvGroupE);
        return ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupDTO.class);
    }

    @Override
    public List<DevopsEnvGroupDTO> listByProject(Long projectId) {
        return ConvertHelper.convertList(devopsEnvGroupRepository.listByProjectId(projectId), DevopsEnvGroupDTO.class);
    }

    @Override
    public Boolean checkName(String name, Long projectId) {
        DevopsEnvGroupE devopsEnvGroupE = devopsEnvGroupRepository.queryByProjectIdAndName(name, projectId);
        if (devopsEnvGroupE != null) {
            return false;
        }
        return true;
    }
}
