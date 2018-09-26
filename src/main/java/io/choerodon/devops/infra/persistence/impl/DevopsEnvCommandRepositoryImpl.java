package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommandDO;
import io.choerodon.devops.infra.mapper.DevopsCommandEventMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandLogMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;

/**
 * @author crcokitwood
 */
@Service
public class DevopsEnvCommandRepositoryImpl implements DevopsEnvCommandRepository {
    private static final String INSTANCE_TYPE = "instance";

    private DevopsEnvCommandMapper devopsEnvCommandMapper;
    private DevopsEnvCommandLogMapper devopsEnvCommandLogMapper;
    private DevopsCommandEventMapper commandEventMapper;


    public DevopsEnvCommandRepositoryImpl(DevopsEnvCommandMapper devopsEnvCommandMapper,
                                          DevopsEnvCommandLogMapper devopsEnvCommandLogMapper,
                                          DevopsCommandEventMapper commandEventMapper) {
        this.devopsEnvCommandMapper = devopsEnvCommandMapper;
        this.devopsEnvCommandLogMapper = devopsEnvCommandLogMapper;
        this.commandEventMapper = commandEventMapper;
    }


    @Override
    public DevopsEnvCommandE create(DevopsEnvCommandE devopsEnvCommandE) {
        DevopsEnvCommandDO devopsEnvCommandDO = ConvertHelper.convert(devopsEnvCommandE, DevopsEnvCommandDO.class);
        if (devopsEnvCommandMapper.insert(devopsEnvCommandDO) != 1) {
            throw new CommonException("error.env.command.insert");
        }

        // 删除实例历史日志以及事件记录
        if (INSTANCE_TYPE.equals(devopsEnvCommandDO.getObject())) {
            commandEventMapper.deletePreInstanceCommandEvent(devopsEnvCommandDO.getObjectId());
            devopsEnvCommandLogMapper.deletePreInstanceCommandLog(devopsEnvCommandDO.getObjectId());
        }
        return ConvertHelper.convert(devopsEnvCommandDO, DevopsEnvCommandE.class);
    }

    @Override
    public DevopsEnvCommandE queryByObject(String objectType, Long objectId) {
        return ConvertHelper.convert(
                devopsEnvCommandMapper.queryByObject(objectType, objectId), DevopsEnvCommandE.class);
    }

    @Override
    public DevopsEnvCommandE update(DevopsEnvCommandE devopsEnvCommandE) {
        DevopsEnvCommandDO devopsEnvCommandDO = ConvertHelper.convert(devopsEnvCommandE, DevopsEnvCommandDO.class);
        DevopsEnvCommandDO newDevopsEnvCommandDO = devopsEnvCommandMapper
                .selectByPrimaryKey(devopsEnvCommandDO.getId());
        devopsEnvCommandDO.setObjectVersionNumber(newDevopsEnvCommandDO.getObjectVersionNumber());
        if (devopsEnvCommandMapper.updateByPrimaryKeySelective(devopsEnvCommandDO) != 1) {
            throw new CommonException("error.env.command.update");
        }
        return ConvertHelper.convert(devopsEnvCommandDO, DevopsEnvCommandE.class);
    }

    @Override
    public DevopsEnvCommandE query(Long id) {
        DevopsEnvCommandDO devopsEnvCommandDO = devopsEnvCommandMapper.selectByPrimaryKey(id);
        return ConvertHelper.convert(devopsEnvCommandDO, DevopsEnvCommandE.class);
    }

    @Override
    public List<DevopsEnvCommandE> listByEnvId(Long envId) {
        DevopsEnvCommandDO devopsEnvCommandDO = new DevopsEnvCommandDO();
//        devopsEnvCommandDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsEnvCommandMapper.select(devopsEnvCommandDO), DevopsEnvCommandE.class);
    }

}
