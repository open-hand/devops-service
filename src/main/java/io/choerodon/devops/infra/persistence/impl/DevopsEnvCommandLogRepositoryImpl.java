package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandLogE;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandLogRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommandLogDO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandLogMapper;

/**
 * Created by younger on 2018/4/24.
 */
@Service
public class DevopsEnvCommandLogRepositoryImpl implements DevopsEnvCommandLogRepository {

    private DevopsEnvCommandLogMapper devopsEnvCommandLogMapper;

    public DevopsEnvCommandLogRepositoryImpl(DevopsEnvCommandLogMapper devopsEnvCommandLogMapper) {
        this.devopsEnvCommandLogMapper = devopsEnvCommandLogMapper;
    }


    @Override
    public DevopsEnvCommandLogE create(DevopsEnvCommandLogE devopsEnvCommandLogE) {
        DevopsEnvCommandLogDO devopsEnvCommandLogDO =
                ConvertHelper.convert(devopsEnvCommandLogE, DevopsEnvCommandLogDO.class);
        if (devopsEnvCommandLogMapper.insert(devopsEnvCommandLogDO) != 1) {
            throw new CommonException("error.log.insert");
        }
        return ConvertHelper.convert(devopsEnvCommandLogDO, DevopsEnvCommandLogE.class);
    }

    @Override
    public DevopsEnvCommandLogE query(Long logId) {
        return ConvertHelper.convert(devopsEnvCommandLogMapper.selectByPrimaryKey(logId),
                DevopsEnvCommandLogE.class);
    }

    @Override
    public List<DevopsEnvCommandLogE> queryByDeployId(Long commandId) {
        DevopsEnvCommandLogDO devopsEnvCommandLogDO = new DevopsEnvCommandLogDO();
        devopsEnvCommandLogDO.setCommandId(commandId);
        List<DevopsEnvCommandLogDO> devopsEnvCommandLogDOS = devopsEnvCommandLogMapper.select(devopsEnvCommandLogDO);
        return ConvertHelper.convertList(devopsEnvCommandLogDOS, DevopsEnvCommandLogE.class);
    }

    @Override
    public void deletePreInstanceCommandLog(Long instanceId) {
        devopsEnvCommandLogMapper.deletePreInstanceCommandLog(instanceId);
    }

    @Override
    public void deleteByCommandId(Long commandId) {
        DevopsEnvCommandLogDO devopsEnvCommandLogDO = new DevopsEnvCommandLogDO();
        devopsEnvCommandLogDO.setCommandId(commandId);
        devopsEnvCommandLogMapper.delete(devopsEnvCommandLogDO);
    }
}
