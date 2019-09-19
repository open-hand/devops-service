package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsAppServiceResourceService;
import io.choerodon.devops.infra.dto.DevopsAppServiceResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsAppServiceResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zmf
 */
@Service
public class DevopsAppServiceResourceServiceImpl implements DevopsAppServiceResourceService {
    @Autowired
    private DevopsAppServiceResourceMapper resourceMapper;


    @Override
    public void handleAppServiceResource(List<Long> appServiceIds, Long resourceId, String type) {
        List<Long> oldAppServiceIds = baseQueryByResourceIdAndType(resourceId, type).stream().map(DevopsAppServiceResourceDTO::getAppServiceId).collect(Collectors.toList());
        if (!oldAppServiceIds.isEmpty()) {
            //过滤掉为null的元素
            List<Long> collect = appServiceIds.stream().filter(e -> e != null).collect(Collectors.toList());
            System.out.println(!collect.isEmpty());
            if (collect != null && !collect.isEmpty()) {
                appServiceIds.forEach(aLong -> {
                    if (!oldAppServiceIds.contains(aLong)) {
                        DevopsAppServiceResourceDTO devopsAppServiceResourceDTO = new DevopsAppServiceResourceDTO(aLong, type, resourceId);
                        baseCreate(devopsAppServiceResourceDTO);
                    }
                });
                oldAppServiceIds.forEach(aLong -> {
                    if (!appServiceIds.contains(aLong)) {
                        baseDeleteByResourceIdAndType(resourceId, type);
                    }
                });
            } else {
                oldAppServiceIds.stream().forEach(e -> baseDeleteByResourceIdAndType(resourceId, type));
            }
        } else {
            appServiceIds.forEach(aLong -> {
                DevopsAppServiceResourceDTO devopsAppServiceResourceDTO = new DevopsAppServiceResourceDTO(aLong, type, resourceId);
                baseCreate(devopsAppServiceResourceDTO);
            });
        }
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
}
