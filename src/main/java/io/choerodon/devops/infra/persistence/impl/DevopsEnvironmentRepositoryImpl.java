package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;

/**
 * Created by younger on 2018/4/9.
 */
@Service
public class DevopsEnvironmentRepositoryImpl implements DevopsEnvironmentRepository {

    private DevopsEnvironmentMapper devopsEnvironmentMapper;

    public DevopsEnvironmentRepositoryImpl(DevopsEnvironmentMapper devopsEnvironmentMapper) {
        this.devopsEnvironmentMapper = devopsEnvironmentMapper;
    }

    @Override
    public DevopsEnvironmentE create(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentDO devopsEnvironmentDO = ConvertHelper.convert(devopsEnvironmentE, DevopsEnvironmentDO.class);
        if (devopsEnvironmentMapper.insert(devopsEnvironmentDO) != 1) {
            throw new CommonException("error.environment.create");
        }
        return ConvertHelper.convert(devopsEnvironmentDO, DevopsEnvironmentE.class);
    }

    @Override
    public DevopsEnvironmentE queryById(Long id) {
        return ConvertHelper.convert(devopsEnvironmentMapper.selectByPrimaryKey(id), DevopsEnvironmentE.class);
    }

    @Override
    public Boolean activeEnvironment(Long environmentId, Boolean active) {
        DevopsEnvironmentDO devopsEnvironmentDO = devopsEnvironmentMapper.selectByPrimaryKey(environmentId);
        devopsEnvironmentDO.setId(environmentId);
        devopsEnvironmentDO.setActive(active);
        devopsEnvironmentDO.setObjectVersionNumber(devopsEnvironmentDO.getObjectVersionNumber());
        if (devopsEnvironmentMapper.updateByPrimaryKeySelective(devopsEnvironmentDO) != 1) {
            throw new CommonException("error.environment.update");
        }
        return true;
    }

    @Override
    public DevopsEnvironmentE update(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentDO newDevopsEnvironmentDO = devopsEnvironmentMapper.selectByPrimaryKey(
                devopsEnvironmentE.getId());
        DevopsEnvironmentDO devopsEnvironmentDO = ConvertHelper.convert(devopsEnvironmentE, DevopsEnvironmentDO.class);
        devopsEnvironmentDO.setObjectVersionNumber(newDevopsEnvironmentDO.getObjectVersionNumber());
        if (devopsEnvironmentE.getDevopsEnvGroupId() == null) {
            devopsEnvironmentMapper.updateDevopsEnvGroupId(devopsEnvironmentDO.getId());
        }
        if (devopsEnvironmentMapper.updateByPrimaryKeySelective(devopsEnvironmentDO) != 1) {
            throw new CommonException("error.environment.update");
        }
        return ConvertHelper.convert(devopsEnvironmentDO, DevopsEnvironmentE.class);
    }

    @Override
    public void checkName(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO();
        devopsEnvironmentDO.setClusterId(devopsEnvironmentE.getClusterE().getId());
        devopsEnvironmentDO.setName(devopsEnvironmentE.getName());
        if (!devopsEnvironmentMapper.select(devopsEnvironmentDO).isEmpty()) {
            throw new CommonException("error.name.exist");
        }
    }

    @Override
    public void checkCode(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO();
        devopsEnvironmentDO.setClusterId(devopsEnvironmentE.getClusterE().getId());
        devopsEnvironmentDO.setCode(devopsEnvironmentE.getCode());
        if (!devopsEnvironmentMapper.select(devopsEnvironmentDO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
    }

    @Override
    public List<DevopsEnvironmentE> queryByProject(Long projectId) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO();
        devopsEnvironmentDO.setProjectId(projectId);
        List<DevopsEnvironmentDO> devopsEnvironmentDOS = devopsEnvironmentMapper.select(devopsEnvironmentDO);
        return ConvertHelper.convertList(devopsEnvironmentDOS, DevopsEnvironmentE.class);
    }

    @Override
    public List<DevopsEnvironmentE> queryByprojectAndActive(Long projectId, Boolean active) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO();
        devopsEnvironmentDO.setProjectId(projectId);
        devopsEnvironmentDO.setActive(active);
        List<DevopsEnvironmentDO> devopsEnvironmentDOS = devopsEnvironmentMapper.select(devopsEnvironmentDO);
        return ConvertHelper.convertList(devopsEnvironmentDOS, DevopsEnvironmentE.class);
    }

    @Override
    public DevopsEnvironmentE queryByProjectIdAndCode(Long projectId, String code) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO();
        devopsEnvironmentDO.setProjectId(projectId);
        devopsEnvironmentDO.setCode(code);
        return ConvertHelper.convert(devopsEnvironmentMapper.selectOne(devopsEnvironmentDO), DevopsEnvironmentE.class);
    }

    @Override
    public DevopsEnvironmentE queryByToken(String token) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO();
        devopsEnvironmentDO.setToken(token);
        devopsEnvironmentMapper.selectOne(devopsEnvironmentDO);
        return ConvertHelper.convert(devopsEnvironmentMapper.selectOne(devopsEnvironmentDO), DevopsEnvironmentE.class);
    }

    @Override
    public List<DevopsEnvironmentE> list() {
        return ConvertHelper.convertList(devopsEnvironmentMapper.selectAll(), DevopsEnvironmentE.class);
    }

    @Override
    public void updateEnvCommit(DevopsEnvironmentE devopsEnvironmentE) {
        devopsEnvironmentMapper.updateDevopsEnvCommit(devopsEnvironmentE.getId(),
                devopsEnvironmentE.getSagaSyncCommit(), devopsEnvironmentE.getDevopsSyncCommit(),
                devopsEnvironmentE.getAgentSyncCommit());
    }

    @Override
    public void deleteById(Long id) {
        devopsEnvironmentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<DevopsEnvironmentE> listByClusterId(Long clusterId) {
        DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO();
        devopsEnvironmentDO.setClusterId(clusterId);
        return ConvertHelper.convertList(devopsEnvironmentMapper.select(devopsEnvironmentDO), DevopsEnvironmentE.class);
    }
}
