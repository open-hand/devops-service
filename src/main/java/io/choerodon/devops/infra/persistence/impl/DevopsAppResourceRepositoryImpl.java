package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsAppResourceE;
import io.choerodon.devops.domain.application.repository.DevopsAppResourceRepository;
import io.choerodon.devops.infra.dto.DevopsAppResourceDO;
import io.choerodon.devops.infra.mapper.DevopsAppResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
@Component
public class DevopsAppResourceRepositoryImpl implements DevopsAppResourceRepository {

    @Autowired
    DevopsAppResourceMapper resourceMapper;

    @Override
    public void insert(DevopsAppResourceE devopsAppResourceE) {
        DevopsAppResourceDO resourceDO = ConvertHelper.convert(devopsAppResourceE, DevopsAppResourceDO.class);
        if (resourceMapper.insert(resourceDO) != 1) {
            throw new CommonException("error.insert.app.resource");
        }
    }

//    @Override
//    public DevopsAppResourceE queryByAppId(Long appId) {
//        DevopsAppResourceDO resourceDO = new DevopsAppResourceDO();
//        resourceDO.setAppId(appId);
//        return ConvertHelper.convert(resourceMapper.selectOne(resourceDO),DevopsAppResourceE.class);
//    }

    @Override
    public void deleteByAppIdAndType(Long appId, String type) {
        DevopsAppResourceDO resourceDO = new DevopsAppResourceDO();
        resourceDO.setAppId(appId);
        resourceDO.setResourceType(type);
        resourceMapper.delete(resourceDO);
    }

    @Override
    public void deleteByResourceIdAndType(Long resourceId, String type) {
        DevopsAppResourceDO resourceDO = new DevopsAppResourceDO();
        resourceDO.setResourceId(resourceId);
        resourceDO.setResourceType(type);
        resourceMapper.delete(resourceDO);
    }

    @Override
    public List<DevopsAppResourceE> queryByAppAndType(Long appId, String type) {
        DevopsAppResourceDO resourceDO = new DevopsAppResourceDO();
        resourceDO.setAppId(appId);
        resourceDO.setResourceType(type);
        return ConvertHelper.convertList(resourceMapper.select(resourceDO), DevopsAppResourceE.class);
    }
}
