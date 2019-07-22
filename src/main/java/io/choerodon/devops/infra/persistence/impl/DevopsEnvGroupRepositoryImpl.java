package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.domain.application.repository.DevopsEnvGroupRepository;
import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvGroupMapper;

@Service
public class DevopsEnvGroupRepositoryImpl implements DevopsEnvGroupRepository {

    @Autowired
    private DevopsEnvGroupMapper devopsEnvGroupMapper;

    @Override
    public DevopsEnvGroupE baseCreate(DevopsEnvGroupE devopsEnvGroupE) {
        DevopsEnvGroupDTO devopsEnvGroupDO = ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupDTO.class);
        devopsEnvGroupDO.setSequence(getMaxSequenceInProject(devopsEnvGroupDO.getProjectId()) + 1);
        devopsEnvGroupMapper.insert(devopsEnvGroupDO);
        return ConvertHelper.convert(devopsEnvGroupDO, DevopsEnvGroupE.class);
    }

    @Override
    public DevopsEnvGroupE baseUpdate(DevopsEnvGroupE devopsEnvGroupE) {
        DevopsEnvGroupDTO devopsEnvGroupDO = devopsEnvGroupMapper.selectByPrimaryKey(devopsEnvGroupE.getId());
        DevopsEnvGroupDTO updateDevopsEnvGroupDO = ConvertHelper.convert(devopsEnvGroupE, DevopsEnvGroupDTO.class);
        updateDevopsEnvGroupDO.setObjectVersionNumber(devopsEnvGroupDO.getObjectVersionNumber());
        devopsEnvGroupMapper.updateByPrimaryKeySelective(updateDevopsEnvGroupDO);
        return ConvertHelper.convert(updateDevopsEnvGroupDO, DevopsEnvGroupE.class);
    }

    @Override
    public List<DevopsEnvGroupE> baseListByProjectId(Long projectId) {
        DevopsEnvGroupDTO devopsEnvGroupDO = new DevopsEnvGroupDTO();
        devopsEnvGroupDO.setProjectId(projectId);
        return ConvertHelper.convertList(devopsEnvGroupMapper.select(devopsEnvGroupDO), DevopsEnvGroupE.class);
    }

    @Override
    public DevopsEnvGroupE baseQuery(Long id) {
        return ConvertHelper.convert(devopsEnvGroupMapper.selectByPrimaryKey(id), DevopsEnvGroupE.class);
    }


    @Override
    public Boolean baseCheckUniqueInProject(Long id, String name, Long projectId) {
        DevopsEnvGroupDTO devopsEnvGroupDO = new DevopsEnvGroupDTO();
        devopsEnvGroupDO.setName(name);
        devopsEnvGroupDO.setProjectId(projectId);
        List<DevopsEnvGroupDTO> devopsEnvGroupDOS = devopsEnvGroupMapper.select(devopsEnvGroupDO);
        boolean updateCheck = false;
        if (id != null) {
            updateCheck = devopsEnvGroupDOS.size() == 1 && id.equals(devopsEnvGroupDOS.get(0).getId());
        }
        return devopsEnvGroupDOS.isEmpty() || updateCheck;
    }

    @Override
    public Boolean baseCheckUniqueInProject(String name, Long projectId) {
        return baseCheckUniqueInProject(null, name, projectId);
    }

    @Override
    public void baseDelete(Long id) {
        devopsEnvGroupMapper.deleteByPrimaryKey(id);
    }

    private Long getMaxSequenceInProject(Long projectId) {
        DevopsEnvGroupDTO devopsEnvGroupDO = new DevopsEnvGroupDTO();
        devopsEnvGroupDO.setProjectId(projectId);
        List<DevopsEnvGroupDTO> devopsEnvGroupDOS = devopsEnvGroupMapper.select(devopsEnvGroupDO);
        return devopsEnvGroupDOS.stream().map(DevopsEnvGroupDTO::getSequence).max(Long::compareTo).orElse(0L);
    }
}
