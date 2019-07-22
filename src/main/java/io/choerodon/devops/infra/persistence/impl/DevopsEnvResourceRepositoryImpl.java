package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.devops.infra.enums.ResourceType;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsEnvResourceRepository;
import io.choerodon.devops.infra.dto.DevopsEnvResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvResourceMapper;

/**
 * Created by younger on 2018/4/24.
 */
@Service
public class DevopsEnvResourceRepositoryImpl implements DevopsEnvResourceRepository {

    private DevopsEnvResourceMapper devopsEnvResourceMapper;

    public DevopsEnvResourceRepositoryImpl(DevopsEnvResourceMapper devopsEnvResourceMapper) {
        this.devopsEnvResourceMapper = devopsEnvResourceMapper;
    }

    @Override
    public void baseCreate(DevopsEnvResourceE devopsEnvResourceE) {
        DevopsEnvResourceDTO devopsEnvResourceDO =
                ConvertHelper.convert(devopsEnvResourceE, DevopsEnvResourceDTO.class);
        if (devopsEnvResourceMapper.insert(devopsEnvResourceDO) != 1) {
            throw new CommonException("error.resource.insert");
        }
    }

    @Override
    public List<DevopsEnvResourceE> baseListByInstanceId(Long instanceId) {
        DevopsEnvResourceDTO devopsEnvResourceDO = new DevopsEnvResourceDTO();
        devopsEnvResourceDO.setAppInstanceId(instanceId);
        List<DevopsEnvResourceDTO> devopsEnvResourceDOS = devopsEnvResourceMapper.select(
                devopsEnvResourceDO);
        return ConvertHelper.convertList(devopsEnvResourceDOS, DevopsEnvResourceE.class);
    }

    @Override
    public List<DevopsEnvResourceE> baseListByCommandId(Long commandId) {
        return ConvertHelper.convertList(
                devopsEnvResourceMapper.listJobs(commandId),
                DevopsEnvResourceE.class);
    }

    @Override
    public void baseUpdate(DevopsEnvResourceE devopsEnvResourceE) {
        DevopsEnvResourceDTO devopsEnvResourceDO = ConvertHelper.convert(
                devopsEnvResourceE, DevopsEnvResourceDTO.class);
        devopsEnvResourceDO.setObjectVersionNumber(
                devopsEnvResourceMapper.selectByPrimaryKey(
                        devopsEnvResourceDO.getId()).getObjectVersionNumber());
        if (devopsEnvResourceMapper.updateByPrimaryKeySelective(devopsEnvResourceDO) != 1) {
            throw new CommonException("error.resource.update");
        }
    }

    @Override
    public void deleteByEnvIdAndKindAndName(Long envId, String kind, String name) {
        DevopsEnvResourceDTO devopsEnvResourceDO = new DevopsEnvResourceDTO();
        if (devopsEnvResourceMapper.queryResource(null, null, envId, kind, name) != null) {
            devopsEnvResourceDO.setEnvId(envId);
        }
        devopsEnvResourceDO.setKind(kind);
        devopsEnvResourceDO.setName(name);
        devopsEnvResourceMapper.delete(devopsEnvResourceDO);
    }

    @Override
    public List<DevopsEnvResourceE> baseListByEnvAndType(Long envId, String type) {
        return ConvertHelper.convertList(
                devopsEnvResourceMapper.listByEnvAndType(envId, type), DevopsEnvResourceE.class);
    }

    @Override
    public DevopsEnvResourceE baseQueryByKindAndName(String kind, String name) {
        return ConvertHelper.convert(devopsEnvResourceMapper.queryLatestJob(kind, name), DevopsEnvResourceE.class);
    }

    @Override
    public void deleteByKindAndNameAndInstanceId(String kind, String name, Long instanceId) {
        DevopsEnvResourceDTO devopsEnvResourceDO = new DevopsEnvResourceDTO();
        devopsEnvResourceDO.setKind(kind);
        devopsEnvResourceDO.setName(name);
        devopsEnvResourceDO.setAppInstanceId(instanceId);
        devopsEnvResourceMapper.delete(devopsEnvResourceDO);
    }

    @Override
    public DevopsEnvResourceE baseQueryOptions(Long instanceId, Long commandId, Long envId, String kind, String name) {
        return ConvertHelper.convert(devopsEnvResourceMapper.queryResource(instanceId, commandId, envId, kind, name), DevopsEnvResourceE.class);
    }


    @Override
    public String getResourceDetailByNameAndTypeAndInstanceId(Long instanceId, String name, ResourceType resourceType) {
        return devopsEnvResourceMapper.getResourceDetailByNameAndTypeAndInstanceId(instanceId, name, resourceType.getType());
    }
}
