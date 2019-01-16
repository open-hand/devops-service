package io.choerodon.devops.infra.persistence.impl;

import java.util.Date;
import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommandDO;
import io.choerodon.devops.infra.mapper.DevopsCommandEventMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandLogMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.stereotype.Service;

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
        return ConvertHelper.convertList(devopsEnvCommandMapper.select(devopsEnvCommandDO), DevopsEnvCommandE.class);
    }

    @Override
    public List<DevopsEnvCommandE> queryInstanceCommand(String objectType, Long objectId) {
        return ConvertHelper.convertList(devopsEnvCommandMapper.queryInstanceCommand(objectType, objectId), DevopsEnvCommandE.class);
    }

    @Override
    public Page<DevopsEnvCommandE> listByObject(PageRequest pageRequest, String objectType, Long objectId, Date startTime, Date endTime) {
        Page<ApplicationInstanceDO> applicationInstanceDOPage = PageHelper.doPageAndSort(pageRequest, () ->
                devopsEnvCommandMapper.listByObject(objectType, objectId, startTime == null ? null : new java.sql.Date(startTime.getTime()), endTime == null ? null : new java.sql.Date(endTime.getTime())));
        return ConvertPageHelper.convertPage(applicationInstanceDOPage, DevopsEnvCommandE.class);
    }
}
