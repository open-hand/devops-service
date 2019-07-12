package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.iam.entity.DevopsCommandEventE;
import io.choerodon.devops.domain.application.repository.DevopsCommandEventRepository;
import io.choerodon.devops.infra.dto.DevopsCommandEventDTO;
import io.choerodon.devops.infra.mapper.DevopsCommandEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevopsCommandEventRepositoryImpl implements DevopsCommandEventRepository {

    @Autowired
    private DevopsCommandEventMapper devopsCommandEventMapper;

    @Override
    public void baseCreate(DevopsCommandEventE devopsCommandEventE) {
        devopsCommandEventMapper.insert(ConvertHelper.convert(devopsCommandEventE, DevopsCommandEventDTO.class));
    }

    @Override
    public List<DevopsCommandEventE> baseListByCommandIdAndType(Long commandId, String type) {
        DevopsCommandEventDTO devopsCommandEventDTO = new DevopsCommandEventDTO();
        devopsCommandEventDTO.setCommandId(commandId);
        devopsCommandEventDTO.setType(type);
        return ConvertHelper.convertList(
                devopsCommandEventMapper.select(devopsCommandEventDTO), DevopsCommandEventE.class);
    }

    @Override
    public void baseDeletePreInstanceCommandEvent(Long instanceId) {
        devopsCommandEventMapper.deletePreInstanceCommandEvent(instanceId);
    }

    @Override
    public void baseDeleteByCommandId(Long commandId) {
        DevopsCommandEventDTO devopsCommandEventDTO = new DevopsCommandEventDTO();
        devopsCommandEventDTO.setCommandId(commandId);
        devopsCommandEventMapper.delete(devopsCommandEventDTO);
    }
}
