package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsApplicationResourceService;
import io.choerodon.devops.infra.dto.DevopsApplicationResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsApplicationResourceMapper;

/**
 * @author zmf
 */
@Service
public class DevopsApplicationResourceServiceImpl implements DevopsApplicationResourceService {
    @Autowired
    private DevopsApplicationResourceMapper resourceMapper;

    @Override
    public void baseCreate(DevopsApplicationResourceDTO devopsApplicationResourceDTO) {
        if (resourceMapper.insert(devopsApplicationResourceDTO) != 1) {
            throw new CommonException("error.insert.app.resource");
        }
    }

    @Override
    public void baseDeleteByAppIdAndType(Long appId, String type) {
        DevopsApplicationResourceDTO resourceDO = new DevopsApplicationResourceDTO();
        resourceDO.setAppServiceId(appId);
        resourceDO.setResourceType(type);
        resourceMapper.delete(resourceDO);
    }

    @Override
    public void baseDeleteByResourceIdAndType(Long resourceId, String type) {
        DevopsApplicationResourceDTO resourceDO = new DevopsApplicationResourceDTO();
        resourceDO.setResourceId(resourceId);
        resourceDO.setResourceType(type);
        resourceMapper.delete(resourceDO);
    }

    @Override
    public List<DevopsApplicationResourceDTO> baseQueryByApplicationAndType(Long appId, String type) {
        DevopsApplicationResourceDTO resourceDO = new DevopsApplicationResourceDTO();
        resourceDO.setAppServiceId(appId);
        resourceDO.setResourceType(type);
        return resourceMapper.select(resourceDO);
    }
}
