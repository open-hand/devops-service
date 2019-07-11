package io.choerodon.devops.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandValueE;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandValueRepository;
import io.choerodon.devops.infra.dto.DevopsEnvCommandValueDO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandValueMapper;

@Service
public class DevopsEnvCommandValueRepositoryImpl implements DevopsEnvCommandValueRepository {

    private DevopsEnvCommandValueMapper devopsEnvCommandValueMapper;

    public DevopsEnvCommandValueRepositoryImpl(DevopsEnvCommandValueMapper devopsEnvCommandValueMapper) {
        this.devopsEnvCommandValueMapper = devopsEnvCommandValueMapper;
    }

    @Override
    public DevopsEnvCommandValueE create(DevopsEnvCommandValueE devopsEnvCommandValueE) {
        DevopsEnvCommandValueDO devopsEnvCommandValueDO = ConvertHelper
                .convert(devopsEnvCommandValueE, DevopsEnvCommandValueDO.class);
        if (devopsEnvCommandValueMapper.insert(devopsEnvCommandValueDO) != 1) {
            throw new CommonException("error.env.command.value.insert");
        }
        return ConvertHelper.convert(devopsEnvCommandValueDO, DevopsEnvCommandValueE.class);
    }

    @Override
    public void deleteById(Long valueId) {
        DevopsEnvCommandValueDO devopsEnvCommandValueDO = new DevopsEnvCommandValueDO();
        devopsEnvCommandValueDO.setId(valueId);
        devopsEnvCommandValueMapper.deleteByPrimaryKey(devopsEnvCommandValueDO);
    }

    @Override
    public void updateValueById(Long valueId, String value)
    {
        DevopsEnvCommandValueDO devopsEnvCommandValueDO = new DevopsEnvCommandValueDO();
        devopsEnvCommandValueDO.setId(valueId);
        devopsEnvCommandValueDO.setValue(value);
        devopsEnvCommandValueMapper.updateByPrimaryKeySelective(devopsEnvCommandValueDO);
    }
}
