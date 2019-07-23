package io.choerodon.devops.infra.persistence.impl;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandValueRepository;
import io.choerodon.devops.infra.dto.DevopsEnvCommandValueDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandValueMapper;

@Service
public class DevopsEnvCommandValueRepositoryImpl implements DevopsEnvCommandValueRepository {

    private DevopsEnvCommandValueMapper devopsEnvCommandValueMapper;

    public DevopsEnvCommandValueRepositoryImpl(DevopsEnvCommandValueMapper devopsEnvCommandValueMapper) {
        this.devopsEnvCommandValueMapper = devopsEnvCommandValueMapper;
    }

    @Override
    public DevopsEnvCommandValueVO baseCreate(DevopsEnvCommandValueVO devopsEnvCommandValueE) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDO = ConvertHelper
                .convert(devopsEnvCommandValueE, DevopsEnvCommandValueDTO.class);
        if (devopsEnvCommandValueMapper.insert(devopsEnvCommandValueDO) != 1) {
            throw new CommonException("error.env.command.value.insert");
        }
        return ConvertHelper.convert(devopsEnvCommandValueDO, DevopsEnvCommandValueVO.class);
    }

    @Override
    public void baseDeleteById(Long valueId) {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDO.setId(valueId);
        devopsEnvCommandValueMapper.deleteByPrimaryKey(devopsEnvCommandValueDO);
    }

    @Override
    public void baseUpdateById(Long valueId, String value)
    {
        DevopsEnvCommandValueDTO devopsEnvCommandValueDO = new DevopsEnvCommandValueDTO();
        devopsEnvCommandValueDO.setId(valueId);
        devopsEnvCommandValueDO.setValue(value);
        devopsEnvCommandValueMapper.updateByPrimaryKeySelective(devopsEnvCommandValueDO);
    }
}
