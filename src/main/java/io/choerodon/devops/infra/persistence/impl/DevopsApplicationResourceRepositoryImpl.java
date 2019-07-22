package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsApplicationResourceRepository;
import io.choerodon.devops.infra.dto.DevopsApplicationResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsApplicationResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
@Component
public class DevopsApplicationResourceRepositoryImpl implements DevopsApplicationResourceRepository {

    @Autowired
    DevopsApplicationResourceMapper resourceMapper;

    @Override
    public void baseCreate(DevopsAppResourceE devopsAppResourceE) {
        DevopsApplicationResourceDTO resourceDO = ConvertHelper.convert(devopsAppResourceE, DevopsApplicationResourceDTO.class);
        if (resourceMapper.insert(resourceDO) != 1) {
            throw new CommonException("error.insert.app.resource");
        }
    }

//    @Override
//    public DevopsAppResourceE queryByAppId(Long appId) {
//        DevopsApplicationResourceDTO resourceDO = new DevopsApplicationResourceDTO();
//        resourceDO.setAppId(appId);
//        return ConvertHelper.convert(resourceMapper.selectOne(resourceDO),DevopsAppResourceE.class);
//    }

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
