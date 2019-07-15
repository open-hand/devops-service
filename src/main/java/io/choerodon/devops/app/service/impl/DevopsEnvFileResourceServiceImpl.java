package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileResourceMapper;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class DevopsEnvFileResourceServiceImpl implements DevopsEnvFileResourceService {

    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper;

    @Override
    public void updateOrCreateFileResource(Map<String, String> objectPath,
                                           Long envId,
                                           DevopsEnvFileResourceVO devopsEnvFileResourceE,
                                           Integer i, Long id, String kind) {
        if (devopsEnvFileResourceE != null) {
            devopsEnvFileResourceE.setFilePath(objectPath.get(
                    TypeUtil.objToString(i)));
            devopsEnvFileResourceRepository.baseUpdate(devopsEnvFileResourceE);
        } else {
            devopsEnvFileResourceE = new DevopsEnvFileResourceVO();
            devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
            devopsEnvFileResourceE.setFilePath(objectPath.get(
                    TypeUtil.objToString(i)));
            devopsEnvFileResourceE.setResourceId(id);
            devopsEnvFileResourceE.setResourceType(kind);
            devopsEnvFileResourceRepository.baseCreate(devopsEnvFileResourceE);
        }
    }


    @Override
    public DevopsEnvFileResourceDTO baseCreate(DevopsEnvFileResourceDTO devopsEnvFileResourceDTO) {
        devopsEnvFileResourceMapper.insert(devopsEnvFileResourceDTO);
        return devopsEnvFileResourceDTO;
    }

    @Override
    public DevopsEnvFileResourceDTO baseQuery(Long fileResourceId) {
        return devopsEnvFileResourceMapper.selectByPrimaryKey(fileResourceId);
    }

    @Override
    public DevopsEnvFileResourceDTO baseUpdate(DevopsEnvFileResourceDTO devopsEnvFileResourceDTO) {
        devopsEnvFileResourceMapper.updateByPrimaryKeySelective(devopsEnvFileResourceDTO);
        return devopsEnvFileResourceDTO;
    }

    @Override
    public void baseDelete(Long fileResourceId) {
        devopsEnvFileResourceMapper.deleteByPrimaryKey(fileResourceId);
    }

    @Override
    public DevopsEnvFileResourceDTO baseQueryByEnvIdAndResourceId(Long envId, Long resourceId, String resourceType) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDO = new DevopsEnvFileResourceDTO();
        devopsEnvFileResourceDO.setEnvId(envId);
        devopsEnvFileResourceDO.setResourceId(resourceId);
        devopsEnvFileResourceDO.setResourceType(resourceType);
        return devopsEnvFileResourceMapper.selectOne(devopsEnvFileResourceDO);
    }

    @Override
    public List<DevopsEnvFileResourceDTO> baseQueryByEnvIdAndPath(Long envId, String path) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = new DevopsEnvFileResourceDTO();
        devopsEnvFileResourceDTO.setEnvId(envId);
        devopsEnvFileResourceDTO.setFilePath(path);
        return devopsEnvFileResourceMapper.select(devopsEnvFileResourceDTO);
    }

    @Override
    public void baseDeleteByEnvIdAndResourceId(Long envId, Long resourceId, String resourceType) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = new DevopsEnvFileResourceDTO();
        devopsEnvFileResourceDTO.setEnvId(envId);
        devopsEnvFileResourceDTO.setResourceId(resourceId);
        devopsEnvFileResourceDTO.setResourceType(resourceType);
        devopsEnvFileResourceMapper.delete(devopsEnvFileResourceDTO);
    }
}
