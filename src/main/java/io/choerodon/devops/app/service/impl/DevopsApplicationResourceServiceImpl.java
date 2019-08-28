package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsApplicationResourceService;
import io.choerodon.devops.infra.dto.DevopsAppServiceResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsAppServiceResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zmf
 */
@Service
public class DevopsApplicationResourceServiceImpl implements DevopsApplicationResourceService {
    @Autowired
    private DevopsAppServiceResourceMapper resourceMapper;


    @Override
    public void handleAppServiceResource(List<Long> appServiceIds, Long resourceId, String type) {
        List<Long> oldAppServiceIds = baseQueryByResourceIdAndType(resourceId, type).stream().map(DevopsAppServiceResourceDTO::getAppServiceId).collect(Collectors.toList());
        appServiceIds.forEach(aLong -> {
            if (!oldAppServiceIds.contains(aLong)) {
                DevopsAppServiceResourceDTO devopsAppServiceResourceDTO = new DevopsAppServiceResourceDTO(aLong, type, resourceId);
                baseCreate(devopsAppServiceResourceDTO);
            }
        });
        oldAppServiceIds.forEach(aLong -> {
            if (!appServiceIds.contains(aLong)) {
                baseDeleteByResourceIdAndType(resourceId,type);
            }
        });
    }


    @Override
    public void baseCreate(DevopsAppServiceResourceDTO devopsAppServiceResourceDTO) {
        if (resourceMapper.insert(devopsAppServiceResourceDTO) != 1) {
            throw new CommonException("error.insert.app.resource");
        }
    }

    @Override
    public void baseDeleteByResourceIdAndType(Long resourceId, String type) {
        DevopsAppServiceResourceDTO resourceDO = new DevopsAppServiceResourceDTO();
        resourceDO.setResourceId(resourceId);
        resourceDO.setResourceType(type);
        resourceMapper.delete(resourceDO);
    }


    @Override
    public List<DevopsAppServiceResourceDTO> baseQueryByResourceIdAndType(Long resourceId, String type) {
        DevopsAppServiceResourceDTO resourceDTO = new DevopsAppServiceResourceDTO();
        resourceDTO.setResourceId(resourceId);
        resourceDTO.setResourceType(type);
        return resourceMapper.select(resourceDTO);
    }


    @Override
    public List<DevopsAppServiceResourceDTO> baseQueryByApplicationAndType(Long appServiceId, String type) {
        DevopsAppServiceResourceDTO resourceDO = new DevopsAppServiceResourceDTO();
        resourceDO.setAppServiceId(appServiceId);
        resourceDO.setResourceType(type);
        return resourceMapper.select(resourceDO);
    }

}
