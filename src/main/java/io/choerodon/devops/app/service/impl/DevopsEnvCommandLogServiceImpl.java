package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsEnvCommandLogService;
import io.choerodon.devops.infra.dto.DevopsEnvCommandLogDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandLogMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:59 2019/7/24
 * Description:
 */
@Service
public class DevopsEnvCommandLogServiceImpl implements DevopsEnvCommandLogService {
    @Autowired
    private DevopsEnvCommandLogMapper devopsEnvCommandLogMapper;

    @Override
    public DevopsEnvCommandLogDTO baseCreate(DevopsEnvCommandLogDTO devopsEnvCommandLogDTO) {
        if (devopsEnvCommandLogMapper.insert(devopsEnvCommandLogDTO) != 1) {
            throw new CommonException("error.log.insert");
        }
        return devopsEnvCommandLogDTO;
    }

    @Override
    public DevopsEnvCommandLogDTO baseQuery(Long logId) {
        return devopsEnvCommandLogMapper.selectByPrimaryKey(logId);
    }

    @Override
    public List<DevopsEnvCommandLogDTO> baseListByDeployId(Long deployId) {
        DevopsEnvCommandLogDTO devopsEnvCommandLogDTO = new DevopsEnvCommandLogDTO();
        devopsEnvCommandLogDTO.setCommandId(deployId);
        return devopsEnvCommandLogMapper.select(devopsEnvCommandLogDTO);
    }

    @Override
    public void baseDeleteByInstanceId(Long instanceId) {
        devopsEnvCommandLogMapper.deletePreInstanceCommandLog(instanceId);
    }

    @Override
    public void baseDeleteByCommandId(Long commandId) {
        DevopsEnvCommandLogDTO devopsEnvCommandLogDTO = new DevopsEnvCommandLogDTO();
        devopsEnvCommandLogDTO.setCommandId(commandId);
        devopsEnvCommandLogMapper.delete(devopsEnvCommandLogDTO);
    }
}
