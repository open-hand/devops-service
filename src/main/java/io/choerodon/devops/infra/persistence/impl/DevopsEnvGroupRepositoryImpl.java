package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvGroupE;
import io.choerodon.devops.domain.application.repository.DevopsEnvGroupRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvGroupDO;
import io.choerodon.devops.infra.mapper.DevopsEnvGroupMapper;

@Service
public class DevopsEnvGroupRepositoryImpl implements DevopsEnvGroupRepository {


    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper;

    @Override
    public DevopsEnvGroupE create(DevopsEnvGroupE devopsEnvGroupE) {
        DevopsEnvGroupDO devopsEnvGroupDO = ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupDO.class);
        devopsEnvGroupMapper.insert(devopsEnvGroupDO);
        return ConvertHelper.convert(devopsEnvGroupDO, DevopsEnvGroupE.class);
    }

    @Override
    public DevopsEnvGroupE update(DevopsEnvGroupE devopsEnvGroupE) {
        DevopsEnvGroupDO devopsEnvGroupDO = devopsEnvGroupMapper.selectByPrimaryKey(devopsEnvGroupE.getId());
        DevopsEnvGroupDO updateDevopsEnvGroupDO = ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupDO.class);
        updateDevopsEnvGroupDO.setObjectVersionNumber(devopsEnvGroupDO.getObjectVersionNumber());
        devopsEnvGroupMapper.updateByPrimaryKeySelective(updateDevopsEnvGroupDO);
        return ConvertHelper.convert(updateDevopsEnvGroupDO, DevopsEnvGroupE.class);
    }

    @Override
    public List<DevopsEnvGroupE> listByProjectId(Long projectId) {
        DevopsEnvGroupDO devopsEnvGroupDO = new DevopsEnvGroupDO();
        devopsEnvGroupDO.setProjectId(projectId);
        return ConvertHelper.convertList(devopsEnvGroupMapper.select(devopsEnvGroupDO), DevopsEnvGroupE.class);
    }

    @Override
    public DevopsEnvGroupE queryByProjectIdAndName(String name, Long projectId) {
        DevopsEnvGroupDO devopsEnvGroupDO = new DevopsEnvGroupDO();
        devopsEnvGroupDO.setProjectId(projectId);
        devopsEnvGroupDO.setName(name);
        return ConvertHelper.convert(devopsEnvGroupMapper.selectOne(devopsEnvGroupDO), DevopsEnvGroupE.class);
    }

    @Override
    public DevopsEnvGroupE query(Long id) {
        return ConvertHelper.convert(devopsEnvGroupMapper.selectByPrimaryKey(id), DevopsEnvGroupE.class);
    }
}
