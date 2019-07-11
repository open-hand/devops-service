package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.iam.entity.DevopsCommandEventE;
import io.choerodon.devops.domain.application.repository.DevopsCommandEventRepository;
import io.choerodon.devops.infra.dto.DevopsCommandEventDO;
import io.choerodon.devops.infra.mapper.DevopsCommandEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void deletePreInstanceCommandEvent(Long instanceId) {
        devopsCommandEventMapper.deletePreInstanceCommandEvent(instanceId);
    }

    @Override
    public void deleteByCommandId(Long commandId) {
        DevopsCommandEventDO devopsCommandEventDO = new DevopsCommandEventDO();
        devopsCommandEventDO.setCommandId(commandId);
        devopsCommandEventMapper.delete(devopsCommandEventDO);
    }
}
