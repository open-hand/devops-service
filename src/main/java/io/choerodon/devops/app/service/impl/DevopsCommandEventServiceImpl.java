package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.devops.app.service.DevopsCommandEventService;
import io.choerodon.devops.infra.dto.DevopsCommandEventDTO;
import io.choerodon.devops.infra.mapper.DevopsCommandEventMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zmf
 */
@Service
public class DevopsCommandEventServiceImpl implements DevopsCommandEventService {
    @Autowired
    private DevopsCommandEventMapper devopsCommandEventMapper;

    @Override
    public void baseCreate(DevopsCommandEventDTO devopsCommandEventDTO) {
        devopsCommandEventMapper.insert(devopsCommandEventDTO);
    }

    @Override
    public List<DevopsCommandEventDTO> baseListByCommandIdAndType(Long commandId, String type) {
        DevopsCommandEventDTO devopsCommandEventDTO = new DevopsCommandEventDTO();
        devopsCommandEventDTO.setCommandId(commandId);
        devopsCommandEventDTO.setType(type);
        return devopsCommandEventMapper.select(devopsCommandEventDTO);
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
