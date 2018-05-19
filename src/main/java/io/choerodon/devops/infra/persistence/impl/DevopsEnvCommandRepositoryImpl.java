package io.choerodon.devops.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommandDO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;

@Service
public class DevopsEnvCommandRepositoryImpl implements DevopsEnvCommandRepository {

    private DevopsEnvCommandMapper devopsEnvCommandMapper;


    public DevopsEnvCommandRepositoryImpl(DevopsEnvCommandMapper devopsEnvCommandMapper) {
        this.devopsEnvCommandMapper = devopsEnvCommandMapper;
    }


    @Override
    public DevopsEnvCommandE create(DevopsEnvCommandE devopsEnvCommandE) {
        DevopsEnvCommandDO devopsEnvCommandDO = ConvertHelper.convert(devopsEnvCommandE, DevopsEnvCommandDO.class);
        if (devopsEnvCommandMapper.insert(devopsEnvCommandDO) != 1) {
            throw new CommonException("error.env.command.insert");
        }
        return ConvertHelper.convert(devopsEnvCommandDO, DevopsEnvCommandE.class);
    }

    @Override
    public DevopsEnvCommandE queryByObject(String objectType, Long objectId) {
        return ConvertHelper.convert(
                devopsEnvCommandMapper.queryByObject(objectType, objectId), DevopsEnvCommandE.class);
    }

    @Override
    public DevopsEnvCommandE update(DevopsEnvCommandE devopsEnvCommandE) {
        DevopsEnvCommandDO devopsEnvCommandDO = ConvertHelper.convert(devopsEnvCommandE, DevopsEnvCommandDO.class);
        DevopsEnvCommandDO newDevopsEnvCommandDO = devopsEnvCommandMapper
                .selectByPrimaryKey(devopsEnvCommandDO.getId());
        devopsEnvCommandDO.setObjectVersionNumber(newDevopsEnvCommandDO.getObjectVersionNumber());
        if (devopsEnvCommandMapper.updateByPrimaryKeySelective(devopsEnvCommandDO) != 1) {
            throw new CommonException("error.env.command.update");
        }
        return ConvertHelper.convert(devopsEnvCommandDO, DevopsEnvCommandE.class);
    }

}
