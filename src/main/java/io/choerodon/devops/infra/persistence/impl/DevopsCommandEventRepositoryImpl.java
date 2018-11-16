package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.entity.DevopsCommandEventE;
import io.choerodon.devops.domain.application.repository.DevopsCommandEventRepository;
import io.choerodon.devops.infra.dataobject.DevopsCommandEventDO;
import io.choerodon.devops.infra.mapper.DevopsCommandEventMapper;

@Service
public class DevopsCommandEventRepositoryImpl implements DevopsCommandEventRepository {

    @Autowired
    private DevopsCommandEventMapper devopsCommandEventMapper;

    @Override
    public void create(DevopsCommandEventE devopsCommandEventE) {
        devopsCommandEventMapper.insert(ConvertHelper.convert(devopsCommandEventE, DevopsCommandEventDO.class));
    }

    @Override
    public List<DevopsCommandEventE> listByCommandIdAndType(Long commandId, String type) {
        DevopsCommandEventDO devopsCommandEventDO = new DevopsCommandEventDO();
        devopsCommandEventDO.setCommandId(commandId);
        devopsCommandEventDO.setType(type);
        return ConvertHelper.convertList(
                devopsCommandEventMapper.select(devopsCommandEventDO), DevopsCommandEventE.class);
    }
}
