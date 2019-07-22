package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandLogVO;
import io.choerodon.devops.app.service.DevopsEnvCommandLogService;
import io.choerodon.devops.infra.dto.DevopsEnvCommandLogDO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandLogMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:49 2019/7/12
 * Description:
 */
@Service
public class DevopsEnvCommandLogServiceImpl implements DevopsEnvCommandLogService {
    @Autowired
    private DevopsEnvCommandLogMapper devopsEnvCommandLogMapper;

    @Override
    public DevopsEnvCommandLogVO baseCreate(DevopsEnvCommandLogVO devopsEnvCommandLogE) {
        DevopsEnvCommandLogDO devopsEnvCommandLogDO =
                ConvertHelper.convert(devopsEnvCommandLogE, DevopsEnvCommandLogDO.class);
        if (devopsEnvCommandLogMapper.insert(devopsEnvCommandLogDO) != 1) {
            throw new CommonException("error.log.insert");
        }
        return ConvertHelper.convert(devopsEnvCommandLogDO, DevopsEnvCommandLogVO.class);
    }

    @Override
    public DevopsEnvCommandLogVO baseQuery(Long logId) {
        return ConvertHelper.convert(devopsEnvCommandLogMapper.selectByPrimaryKey(logId),
                DevopsEnvCommandLogVO.class);
    }

    @Override
    public List<DevopsEnvCommandLogVO> baseListByDeployId(Long commandId) {
        DevopsEnvCommandLogDO devopsEnvCommandLogDO = new DevopsEnvCommandLogDO();
        devopsEnvCommandLogDO.setCommandId(commandId);
        List<DevopsEnvCommandLogDO> devopsEnvCommandLogDOS = devopsEnvCommandLogMapper.select(devopsEnvCommandLogDO);
        return ConvertHelper.convertList(devopsEnvCommandLogDOS, DevopsEnvCommandLogVO.class);
    }

    @Override
    public void baseDeleteByInstanceId(Long instanceId) {
        devopsEnvCommandLogMapper.deletePreInstanceCommandLog(instanceId);
    }

    @Override
    public void baseDeleteByCommandId(Long commandId) {
        DevopsEnvCommandLogDO devopsEnvCommandLogDO = new DevopsEnvCommandLogDO();
        devopsEnvCommandLogDO.setCommandId(commandId);
        devopsEnvCommandLogMapper.delete(devopsEnvCommandLogDO);
    }
}
