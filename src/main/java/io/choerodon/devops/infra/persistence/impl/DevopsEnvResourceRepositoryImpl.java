package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import io.choerodon.devops.infra.common.util.enums.ResourceType;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvResourceE;
import io.choerodon.devops.domain.application.repository.DevopsEnvResourceRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvResourceDO;
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
    public void create(DevopsEnvResourceE devopsEnvResourceE) {
        DevopsEnvResourceDO devopsEnvResourceDO =
                ConvertHelper.convert(devopsEnvResourceE, DevopsEnvResourceDO.class);
        if (devopsEnvResourceMapper.insert(devopsEnvResourceDO) != 1) {
            throw new CommonException("error.resource.insert");
        }
    }

    @Override
    public List<DevopsEnvResourceE> listByInstanceId(Long instanceId) {
        DevopsEnvResourceDO devopsEnvResourceDO = new DevopsEnvResourceDO();
        devopsEnvResourceDO.setAppInstanceId(instanceId);
        List<DevopsEnvResourceDO> devopsEnvResourceDOS = devopsEnvResourceMapper.select(
                devopsEnvResourceDO);
        return ConvertHelper.convertList(devopsEnvResourceDOS, DevopsEnvResourceE.class);
    }

    @Override
    public List<DevopsEnvResourceE> listJobs(Long commandId) {
        return ConvertHelper.convertList(
                devopsEnvResourceMapper.listJobs(commandId),
                DevopsEnvResourceE.class);
    }

    @Override
    public void update(DevopsEnvResourceE devopsEnvResourceE) {
        DevopsEnvResourceDO devopsEnvResourceDO = ConvertHelper.convert(
                devopsEnvResourceE, DevopsEnvResourceDO.class);
        devopsEnvResourceDO.setObjectVersionNumber(
                devopsEnvResourceMapper.selectByPrimaryKey(
                        devopsEnvResourceDO.getId()).getObjectVersionNumber());
        if (devopsEnvResourceMapper.updateByPrimaryKeySelective(devopsEnvResourceDO) != 1) {
            throw new CommonException("error.resource.update");
        }
    }

    @Override
    public void deleteByEnvIdAndKindAndName(Long envId, String kind, String name) {
        DevopsEnvResourceDO devopsEnvResourceDO = new DevopsEnvResourceDO();
        if (devopsEnvResourceMapper.queryResource(null, null, envId, kind, name) != null) {
            devopsEnvResourceDO.setEnvId(envId);
        }
        devopsEnvResourceDO.setKind(kind);
        devopsEnvResourceDO.setName(name);
        devopsEnvResourceMapper.delete(devopsEnvResourceDO);
    }

    @Override
    public List<DevopsEnvResourceE> listByEnvAndType(Long envId, String type) {
        return ConvertHelper.convertList(
                devopsEnvResourceMapper.listByEnvAndType(envId, type), DevopsEnvResourceE.class);
    }

    @Override
    public DevopsEnvResourceE queryLatestJob(String kind, String name) {
        return ConvertHelper.convert(devopsEnvResourceMapper.queryLatestJob(kind, name), DevopsEnvResourceE.class);
    }

    @Override
    public void deleteByKindAndNameAndInstanceId(String kind, String name, Long instanceId) {
        DevopsEnvResourceDO devopsEnvResourceDO = new DevopsEnvResourceDO();
        devopsEnvResourceDO.setKind(kind);
        devopsEnvResourceDO.setName(name);
        devopsEnvResourceDO.setAppInstanceId(instanceId);
        devopsEnvResourceMapper.delete(devopsEnvResourceDO);
    }

    @Override
    public DevopsEnvResourceE queryResource(Long instanceId, Long commandId, Long envId, String kind, String name) {
        return ConvertHelper.convert(devopsEnvResourceMapper.queryResource(instanceId, commandId, envId, kind, name), DevopsEnvResourceE.class);
    }


    @Override
    public String getResourceDetailByNameAndTypeAndInstanceId(Long instanceId, String name, ResourceType resourceType) {
        return devopsEnvResourceMapper.getResourceDetailByNameAndTypeAndInstanceId(instanceId, name, resourceType.getType());
    }
}
