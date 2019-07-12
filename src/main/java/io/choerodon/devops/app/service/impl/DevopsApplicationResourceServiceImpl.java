package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsAppResourceE;
import io.choerodon.devops.app.service.DevopsApplicationResourceService;
import io.choerodon.devops.infra.dto.DevopsApplicationResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsApplicationResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zmf
 */
@Service
public class DevopsApplicationResourceServiceImpl implements DevopsApplicationResourceService {
    @Autowired
    DevopsApplicationResourceMapper resourceMapper;

    @Override
    public void baseCreate(DevopsAppResourceE devopsAppResourceE) {
        DevopsApplicationResourceDTO resourceDO = ConvertHelper.convert(devopsAppResourceE, DevopsApplicationResourceDTO.class);
        if (resourceMapper.insert(resourceDO) != 1) {
            throw new CommonException("error.insert.app.resource");
        }
    }

    @Override
    public void baseDeleteByAppIdAndType(Long appId, String type) {
        DevopsApplicationResourceDTO resourceDO = new DevopsApplicationResourceDTO();
        resourceDO.setAppId(appId);
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
    public List<DevopsAppResourceE> baseQueryByApplicationAndType(Long appId, String type) {
        DevopsApplicationResourceDTO resourceDO = new DevopsApplicationResourceDTO();
        resourceDO.setAppId(appId);
        resourceDO.setResourceType(type);
        return ConvertHelper.convertList(resourceMapper.select(resourceDO), DevopsAppResourceE.class);
    }
}
