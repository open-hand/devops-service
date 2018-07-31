package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvFileResourceDO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileResourceMapper;

/**
 * Creator: Runge
 * Date: 2018/7/25
 * Time: 17:21
 * Description:
 */
@Component
public class DevopsEnvFileResourceRepositoryImpl implements DevopsEnvFileResourceRepository {

    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper;

    @Override
    public DevopsEnvFileResourceE createFileResource(DevopsEnvFileResourceE devopsEnvFileResourceE) {
        DevopsEnvFileResourceDO devopsEnvFileResourceDO =
                ConvertHelper.convert(devopsEnvFileResourceE, DevopsEnvFileResourceDO.class);
        return ConvertHelper.convert(devopsEnvFileResourceDO, DevopsEnvFileResourceE.class);
    }

    @Override
    public DevopsEnvFileResourceE getFileResource(Long fileResourceId) {
        return ConvertHelper.convert(
                devopsEnvFileResourceMapper.selectByPrimaryKey(fileResourceId),
                DevopsEnvFileResourceE.class);
    }

    @Override
    public DevopsEnvFileResourceE updateFileResource(DevopsEnvFileResourceE devopsEnvFileResourceE) {
        DevopsEnvFileResourceDO devopsEnvFileResourceDO =
                ConvertHelper.convert(devopsEnvFileResourceE, DevopsEnvFileResourceDO.class);
        devopsEnvFileResourceMapper.updateByPrimaryKeySelective(devopsEnvFileResourceDO);
        return devopsEnvFileResourceE;
    }

    @Override
    public void deleteFileResource(Long fileResourceId) {
        devopsEnvFileResourceMapper.deleteByPrimaryKey(fileResourceId);
    }
}
