package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

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
    public List<DevopsEnvResourceE> listJobByInstanceId(Long instanceId) {
        return ConvertHelper.convertList(
                devopsEnvResourceMapper.listJobByInstanceId(instanceId),
                DevopsEnvResourceE.class);
    }

    @Override
    public DevopsEnvResourceE queryByInstanceIdAndKindAndName(Long instanceId, String kind, String name) {
        DevopsEnvResourceDO devopsEnvResourceDO =
                devopsEnvResourceMapper.queryByInstanceIdAndKindAndName(instanceId, kind, name);
        return ConvertHelper.convert(devopsEnvResourceDO, DevopsEnvResourceE.class);
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
    public void deleteByKindAndName(String kind, String name) {
        DevopsEnvResourceDO devopsEnvResourceDO = new DevopsEnvResourceDO();
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

}
