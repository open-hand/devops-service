package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileResourceMapper;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class DevopsEnvFileResourceServiceImpl implements DevopsEnvFileResourceService {

    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsEnvFileResourceMapper devopsEnvFileResourceMapper;

    @Override
    public void updateOrCreateFileResource(Map<String, String> objectPath,
                                           Long envId,
                                           DevopsEnvFileResourceDTO devopsEnvFileResourceDTO,
                                           Integer i, Long id, String kind) {
        if (devopsEnvFileResourceDTO != null) {
            devopsEnvFileResourceDTO.setFilePath(objectPath.get(
                    TypeUtil.objToString(i)));
            devopsEnvFileResourceService.baseUpdate(devopsEnvFileResourceDTO);
        } else {
            devopsEnvFileResourceDTO = new DevopsEnvFileResourceDTO();
            devopsEnvFileResourceDTO.setEnvId(envId);
            devopsEnvFileResourceDTO.setFilePath(objectPath.get(
                    TypeUtil.objToString(i)));
            devopsEnvFileResourceDTO.setResourceId(id);
            devopsEnvFileResourceDTO.setResourceType(kind);
            devopsEnvFileResourceService.baseCreate(devopsEnvFileResourceDTO);
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
    public void baseDeleteById(Long fileResourceId) {
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
