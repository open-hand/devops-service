package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
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
        devopsEnvGroupDO.setSequence(getMaxSequenceInProject(devopsEnvGroupDO.getProjectId()) + 1);
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
    public DevopsEnvGroupE query(Long id) {
        return ConvertHelper.convert(devopsEnvGroupMapper.selectByPrimaryKey(id), DevopsEnvGroupE.class);
    }

    @Override
    public void sort(Long projectId, List<Long> envGroupIds) {
        if (!envGroupIds.isEmpty() && envGroupIds.size() == (devopsEnvGroupMapper.selectAll().size())) {
            devopsEnvGroupMapper.sortGroupInProject(projectId, envGroupIds);
        } else {
            throw new CommonException("error.groupSize.illegal");
        }
    }

    @Override
    public Boolean checkUniqueInProject(Long id, String name, Long projectId) {
        DevopsEnvGroupDO devopsEnvGroupDO = new DevopsEnvGroupDO();
        devopsEnvGroupDO.setName(name);
        devopsEnvGroupDO.setProjectId(projectId);
        List<DevopsEnvGroupDO> devopsEnvGroupDOS = devopsEnvGroupMapper.select(devopsEnvGroupDO);
        Boolean updateCheck = false;
        if (id != null) {
            updateCheck = devopsEnvGroupDOS.size() == 1 && id.equals(devopsEnvGroupDOS.get(0).getId());
        }
        return devopsEnvGroupDOS.isEmpty() || updateCheck;
    }

    @Override
    public Boolean checkUniqueInProject(String name, Long projectId) {
        return checkUniqueInProject(null, name, projectId);
    }

    @Override
    public void delete(Long id) {
        devopsEnvGroupMapper.deleteByPrimaryKey(id);
    }

    private Long getMaxSequenceInProject(Long projectId) {
        DevopsEnvGroupDO devopsEnvGroupDO = new DevopsEnvGroupDO();
        devopsEnvGroupDO.setProjectId(projectId);
        List<DevopsEnvGroupDO> devopsEnvGroupDOS = devopsEnvGroupMapper.select(devopsEnvGroupDO);
        return devopsEnvGroupDOS.parallelStream().map(DevopsEnvGroupDO::getSequence).max(Long::compareTo).orElse(0L);
    }
}
