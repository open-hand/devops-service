package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsRegistrySecretE;
import io.choerodon.devops.domain.application.repository.DevopsRegistrySecretRepository;
import io.choerodon.devops.infra.dataobject.DevopsRegistrySecretDO;
import io.choerodon.devops.infra.mapper.DevopsRegistrySecretMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/3/14.
 */

@Service
public class DevopsRegistrySecretRepositoryImpl implements DevopsRegistrySecretRepository {

    @Autowired
    private DevopsRegistrySecretMapper devopsRegistrySecretMapper;

    @Override
    public DevopsRegistrySecretE create(DevopsRegistrySecretE devopsRegistrySecretE) {
        DevopsRegistrySecretDO devopsRegistrySecretDO = ConvertHelper.convert(devopsRegistrySecretE, DevopsRegistrySecretDO.class);
        if (devopsRegistrySecretMapper.insert(devopsRegistrySecretDO) != 1) {
            throw new CommonException("error.registry.secret.create.error");
        }
        return ConvertHelper.convert(devopsRegistrySecretDO, DevopsRegistrySecretE.class);
    }

    @Override
    public DevopsRegistrySecretE query(Long devopsRegistrySecretId) {
        return ConvertHelper.convert(devopsRegistrySecretMapper.selectByPrimaryKey(devopsRegistrySecretId), DevopsRegistrySecretE.class);
    }

    @Override
    public DevopsRegistrySecretE update(DevopsRegistrySecretE devopsRegistrySecretE) {
        DevopsRegistrySecretDO beforeDevopsRegistrySecretDO = devopsRegistrySecretMapper.selectByPrimaryKey(devopsRegistrySecretE.getId());
        DevopsRegistrySecretDO newDevopsRegistrySecretDO = ConvertHelper.convert(devopsRegistrySecretE, DevopsRegistrySecretDO.class);
        newDevopsRegistrySecretDO.setObjectVersionNumber(beforeDevopsRegistrySecretDO.getObjectVersionNumber());
        if (devopsRegistrySecretMapper.updateByPrimaryKeySelective(newDevopsRegistrySecretDO) != 1) {
            throw new CommonException("error.registry.secret.update.error");
        }
        return ConvertHelper.convert(newDevopsRegistrySecretDO, DevopsRegistrySecretE.class);
    }

    @Override
    public DevopsRegistrySecretE queryByEnv(Long envId, Long configId) {
        DevopsRegistrySecretDO devopsRegistrySecretDO = new DevopsRegistrySecretDO();
        devopsRegistrySecretDO.setConfigId(configId);
        devopsRegistrySecretDO.setEnvId(envId);
        return ConvertHelper.convert(devopsRegistrySecretMapper.selectOne(devopsRegistrySecretDO), DevopsRegistrySecretE.class);
    }

    @Override
    public List<DevopsRegistrySecretE> listByConfig(Long configId) {
        DevopsRegistrySecretDO devopsRegistrySecretDO = new DevopsRegistrySecretDO();
        devopsRegistrySecretDO.setConfigId(configId);
        return ConvertHelper.convertList(devopsRegistrySecretMapper.select(devopsRegistrySecretDO), DevopsRegistrySecretE.class);
    }

    @Override
    public DevopsRegistrySecretE queryByName(Long envId, String name) {
        DevopsRegistrySecretDO devopsRegistrySecretDO = new DevopsRegistrySecretDO();
        devopsRegistrySecretDO.setSecretCode(name);
        devopsRegistrySecretDO.setEnvId(envId);
        return ConvertHelper.convert(devopsRegistrySecretMapper.selectOne(devopsRegistrySecretDO), DevopsRegistrySecretE.class);
    }
}
