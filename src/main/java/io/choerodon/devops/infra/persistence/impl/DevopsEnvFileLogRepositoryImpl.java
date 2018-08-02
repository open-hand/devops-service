package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileLogE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileLogRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvFileLogDO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileLogMapper;

@Component
public class DevopsEnvFileLogRepositoryImpl implements DevopsEnvFileLogRepository {

    @Autowired
    DevopsEnvFileLogMapper devopsEnvFileLogMapper;


    @Override
    public void create(DevopsEnvFileLogE devopsEnvFileLogE) {
        DevopsEnvFileLogDO devopsEnvFileLogDO = ConvertHelper.convert(devopsEnvFileLogE, DevopsEnvFileLogDO.class);
        devopsEnvFileLogMapper.insert(devopsEnvFileLogDO);
    }

    @Override
    public List<DevopsEnvFileLogE> listByEnvId(Long envId) {
        DevopsEnvFileLogDO devopsEnvFileLogDO = new DevopsEnvFileLogDO();
        devopsEnvFileLogDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsEnvFileLogMapper.select(devopsEnvFileLogDO), DevopsEnvFileLogE.class);
    }
}
