package io.choerodon.devops.infra.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.entity.DevopsAppWebHookE;
import io.choerodon.devops.domain.application.repository.DevopsAppWebHookRepository;
import io.choerodon.devops.infra.dataobject.DevopsAppWebHookDO;
import io.choerodon.devops.infra.mapper.DevopsAppWebHookMapper;

@Service
public class DevopsAppWebHookRepositoryImpl implements DevopsAppWebHookRepository {

    @Autowired
    private DevopsAppWebHookMapper devopsAppWebHookMapper;

    @Override
    public void createHook(DevopsAppWebHookE devopsAppWebHookE) {
        devopsAppWebHookMapper.insert(ConvertHelper.convert(devopsAppWebHookE, DevopsAppWebHookDO.class));
    }

    @Override
    public DevopsAppWebHookE queryByAppId(Long appId) {
        DevopsAppWebHookDO devopsAppWebHookDO = new DevopsAppWebHookDO();
        devopsAppWebHookDO.setAppId(appId);
        return ConvertHelper.convert(devopsAppWebHookMapper.selectOne(devopsAppWebHookDO), DevopsAppWebHookE.class);
    }
}
