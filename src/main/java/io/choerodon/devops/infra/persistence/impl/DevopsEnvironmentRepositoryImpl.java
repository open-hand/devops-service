package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import org.springframework.stereotype.Service;

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
    public DevopsEnvironmentE baseCreate(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentDTO devopsEnvironmentDO = ConvertHelper.convert(devopsEnvironmentE, DevopsEnvironmentDTO.class);
        if (devopsEnvironmentMapper.insert(devopsEnvironmentDO) != 1) {
            throw new CommonException("error.environment.create");
        }
        return ConvertHelper.convert(devopsEnvironmentDO, DevopsEnvironmentE.class);
    }

    @Override
    public DevopsEnvironmentE baseQueryById(Long id) {
        return ConvertHelper.convert(devopsEnvironmentMapper.selectByPrimaryKey(id), DevopsEnvironmentE.class);
    }

    @Override
    public Boolean baseUpdateActive(Long environmentId, Boolean active) {
        DevopsEnvironmentDTO devopsEnvironmentDO = devopsEnvironmentMapper.selectByPrimaryKey(environmentId);
        devopsEnvironmentDO.setId(environmentId);
        devopsEnvironmentDO.setActive(active);
        devopsEnvironmentDO.setObjectVersionNumber(devopsEnvironmentDO.getObjectVersionNumber());
        if (devopsEnvironmentMapper.updateByPrimaryKeySelective(devopsEnvironmentDO) != 1) {
            throw new CommonException("error.environment.update");
        }
        return true;
    }

    @Override
    public DevopsEnvironmentE baseUpdate(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentDTO newDevopsEnvironmentDO = devopsEnvironmentMapper.selectByPrimaryKey(
                devopsEnvironmentE.getId());
        DevopsEnvironmentDTO devopsEnvironmentDO = ConvertHelper.convert(devopsEnvironmentE, DevopsEnvironmentDTO.class);
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
    public void baseCheckCode(DevopsEnvironmentE devopsEnvironmentE) {
        DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO();
        devopsEnvironmentDO.setClusterId(devopsEnvironmentE.getClusterE().getId());
        devopsEnvironmentDO.setCode(devopsEnvironmentE.getCode());
        if (!devopsEnvironmentMapper.select(devopsEnvironmentDO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
        devopsEnvironmentDO.setClusterId(null);
        devopsEnvironmentDO.setProjectId(devopsEnvironmentE.getProjectE().getId());
        if (!devopsEnvironmentMapper.select(devopsEnvironmentDO).isEmpty()) {
            throw new CommonException("error.code.exist");
        }
    }

    @Override
    public List<DevopsEnvironmentE> baseListByProjectId(Long projectId) {
        DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO();
        devopsEnvironmentDO.setProjectId(projectId);
        List<DevopsEnvironmentDTO> devopsEnvironmentDOS = devopsEnvironmentMapper.select(devopsEnvironmentDO);
        return ConvertHelper.convertList(devopsEnvironmentDOS, DevopsEnvironmentE.class);
    }

    @Override
    public List<DevopsEnvironmentE> baseListByProjectIdAndActive(Long projectId, Boolean active) {
        DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO();
        devopsEnvironmentDO.setProjectId(projectId);
        devopsEnvironmentDO.setActive(active);
        List<DevopsEnvironmentDTO> devopsEnvironmentDOS = devopsEnvironmentMapper.select(devopsEnvironmentDO);
        return ConvertHelper.convertList(devopsEnvironmentDOS, DevopsEnvironmentE.class);
    }

    @Override
    public DevopsEnvironmentE baseQueryByClusterIdAndCode(Long clusterId, String code) {
        DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO();
        devopsEnvironmentDO.setClusterId(clusterId);
        devopsEnvironmentDO.setCode(code);
        return ConvertHelper.convert(devopsEnvironmentMapper.selectOne(devopsEnvironmentDO), DevopsEnvironmentE.class);
    }

    @Override
    public DevopsEnvironmentE baseQueryByProjectIdAndCode(Long projectId, String code) {
        DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO();
        devopsEnvironmentDO.setProjectId(projectId);
        devopsEnvironmentDO.setCode(code);
        return ConvertHelper.convert(devopsEnvironmentMapper.selectOne(devopsEnvironmentDO), DevopsEnvironmentE.class);
    }

    @Override
    public DevopsEnvironmentE baseQueryByToken(String token) {
        return ConvertHelper.convert(devopsEnvironmentMapper.queryByToken(token), DevopsEnvironmentE.class);
    }

    @Override
    public List<DevopsEnvironmentE> baseListAll() {
        return ConvertHelper.convertList(devopsEnvironmentMapper.selectAll(), DevopsEnvironmentE.class);
    }

    @Override
    public void baseUpdateSagaSyncEnvCommit(DevopsEnvironmentE devopsEnvironmentE) {
        devopsEnvironmentMapper.updateSagaSyncEnvCommit(devopsEnvironmentE.getId(),
                devopsEnvironmentE.getSagaSyncCommit());
    }

    @Override
    public void baseUpdateDevopsSyncEnvCommit(DevopsEnvironmentE devopsEnvironmentE) {
        devopsEnvironmentMapper.updateDevopsSyncEnvCommit(devopsEnvironmentE.getId(),
                devopsEnvironmentE.getDevopsSyncCommit());
    }

    @Override
    public void baseUpdateAgentSyncEnvCommit(DevopsEnvironmentE devopsEnvironmentE) {
        devopsEnvironmentMapper.updateAgentSyncEnvCommit(devopsEnvironmentE.getId(),
                devopsEnvironmentE.getAgentSyncCommit());
    }


    @Override
    public void baseDeleteById(Long id) {
        devopsEnvironmentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<DevopsEnvironmentE> baseListByClusterId(Long clusterId) {
        DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO();
        devopsEnvironmentDO.setClusterId(clusterId);
        return ConvertHelper.convertList(devopsEnvironmentMapper.select(devopsEnvironmentDO), DevopsEnvironmentE.class);
    }

    @Override
    public void updateOptions(DevopsEnvironmentE devopsEnvironmentE) {
        devopsEnvironmentMapper.updateOptions(devopsEnvironmentE.getGitlabEnvProjectId(),devopsEnvironmentE.getHookId(),devopsEnvironmentE.getSynchro(),devopsEnvironmentE.getId());
    }

}
