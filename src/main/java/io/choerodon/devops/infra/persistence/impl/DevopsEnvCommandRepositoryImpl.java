package io.choerodon.devops.infra.persistence.impl;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvCommandE;
import io.choerodon.devops.domain.application.repository.DevopsCommandEventRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandLogRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvCommandValueRepository;
import io.choerodon.devops.infra.common.util.PageRequestUtil;
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO;
import io.choerodon.devops.infra.dataobject.DevopsEnvCommandDO;
import io.choerodon.devops.infra.mapper.DevopsEnvCommandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author crcokitwood
 */
@Service
public class DevopsEnvCommandRepositoryImpl implements DevopsEnvCommandRepository {
    private static final String INSTANCE_TYPE = "instance";

    @Autowired
    DevopsEnvCommandValueRepository devopsEnvCommandValueRepository;
    @Autowired
    DevopsEnvCommandLogRepository devopsEnvCommandLogRepository;
    @Autowired
    DevopsCommandEventRepository devopsCommandEventRepository;
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;


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
    public PageInfo<DevopsEnvCommandE> listByObject(PageRequest pageRequest, String objectType, Long objectId, Date startTime, Date endTime) {
        PageInfo<ApplicationInstanceDO> applicationInstanceDOPage = PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() ->
                devopsEnvCommandMapper.listByObject(objectType, objectId, startTime == null ? null : new java.sql.Date(startTime.getTime()), endTime == null ? null : new java.sql.Date(endTime.getTime())));
        return ConvertPageHelper.convertPageInfo(applicationInstanceDOPage, DevopsEnvCommandE.class);
    }

    @Override
    public void deleteById(Long commandId) {
        DevopsEnvCommandDO devopsEnvCommandDO = new DevopsEnvCommandDO();
        devopsEnvCommandDO.setId(commandId);
        devopsEnvCommandMapper.deleteByPrimaryKey(devopsEnvCommandDO);
    }

    @Override
    public List<DevopsEnvCommandE> listByObjectAll(String objectType, Long objectId) {
        DevopsEnvCommandDO devopsEnvCommandDO = new DevopsEnvCommandDO();
        devopsEnvCommandDO.setObjectId(objectId);
        devopsEnvCommandDO.setObject(objectType);
        return ConvertHelper.convertList(devopsEnvCommandMapper.select(devopsEnvCommandDO), DevopsEnvCommandE.class);
    }

    @Override
    public void deleteCommandById(DevopsEnvCommandE commandE) {
        if (commandE.getDevopsEnvCommandValueE() != null) {
            devopsEnvCommandValueRepository.deleteById(commandE.getDevopsEnvCommandValueE().getId());
        }
        devopsEnvCommandLogRepository.deleteByCommandId(commandE.getId());
        devopsCommandEventRepository.deleteByCommandId(commandE.getId());
        devopsEnvCommandMapper.deleteByPrimaryKey(commandE.getId());
    }
}
