package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsApplicationResourceService;
import io.choerodon.devops.infra.dto.DevopsAppServiceResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsAppServiceResourceMapper;

/**
 * @author zmf
 */
@Service
public class DevopsApplicationResourceServiceImpl implements DevopsApplicationResourceService {
    @Autowired
    private DevopsAppServiceResourceMapper resourceMapper;

    @Override
    public void baseCreate(DevopsAppServiceResourceDTO devopsAppServiceResourceDTO) {
        if (resourceMapper.insert(devopsAppServiceResourceDTO) != 1) {
            throw new CommonException("error.insert.app.resource");
        }
    }

    @Override
    public void baseDeleteByAppIdAndType(Long appServiceId, String type) {
        DevopsAppServiceResourceDTO resourceDO = new DevopsAppServiceResourceDTO();
        resourceDO.setAppServiceId(appServiceId);
        resourceDO.setResourceType(type);
        resourceMapper.delete(resourceDO);
    }

    @Override
    public void baseDeleteByResourceIdAndType(Long resourceId, String type) {
        DevopsAppServiceResourceDTO resourceDO = new DevopsAppServiceResourceDTO();
        resourceDO.setResourceId(resourceId);
        resourceDO.setResourceType(type);
        resourceMapper.delete(resourceDO);
    }

    @Override
    public List<DevopsAppServiceResourceDTO> baseQueryByApplicationAndType(Long appServiceId, String type) {
        DevopsAppServiceResourceDTO resourceDO = new DevopsAppServiceResourceDTO();
        resourceDO.setAppServiceId(appServiceId);
        resourceDO.setResourceType(type);
        return resourceMapper.select(resourceDO);
    }
}
