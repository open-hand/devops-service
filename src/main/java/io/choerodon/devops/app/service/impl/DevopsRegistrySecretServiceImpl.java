package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsRegistrySecretService;
import io.choerodon.devops.infra.dto.DevopsRegistrySecretDTO;
import io.choerodon.devops.infra.mapper.DevopsRegistrySecretMapper;

/**
 * Created by Sheep on 2019/7/15.
 */

@Service
public class DevopsRegistrySecretServiceImpl implements DevopsRegistrySecretService {


    @Autowired
    private DevopsRegistrySecretMapper devopsRegistrySecretMapper;

    @Override
    public void deleteByEnvId(Long envId) {
        DevopsRegistrySecretDTO deleteCondition = new DevopsRegistrySecretDTO();
        deleteCondition.setEnvId(Objects.requireNonNull(envId));
        devopsRegistrySecretMapper.delete(deleteCondition);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DevopsRegistrySecretDTO baseCreate(DevopsRegistrySecretDTO devopsRegistrySecretDTO) {
        if (devopsRegistrySecretMapper.insert(devopsRegistrySecretDTO) != 1) {
            throw new CommonException("error.registry.secret.create.error");
        }
        return devopsRegistrySecretDTO;
    }

    @Override
    public DevopsRegistrySecretDTO baseQuery(Long devopsRegistrySecretId) {
        return devopsRegistrySecretMapper.selectByPrimaryKey(devopsRegistrySecretId);
    }

    @Override
    public DevopsRegistrySecretDTO baseUpdate(DevopsRegistrySecretDTO devopsRegistrySecretDTO) {
        DevopsRegistrySecretDTO beforeDevopsRegistrySecretDTO = devopsRegistrySecretMapper.selectByPrimaryKey(devopsRegistrySecretDTO.getId());
        devopsRegistrySecretDTO.setObjectVersionNumber(beforeDevopsRegistrySecretDTO.getObjectVersionNumber());
        if (devopsRegistrySecretMapper.updateByPrimaryKeySelective(devopsRegistrySecretDTO) != 1) {
            throw new CommonException("error.registry.secret.update.error");
        }
        return beforeDevopsRegistrySecretDTO;
    }

    @Override
    public void baseUpdateStatus(Long id, Boolean status) {
        devopsRegistrySecretMapper.updateStatus(id, status);
    }

    @Override
    public DevopsRegistrySecretDTO baseQueryByClusterIdAndNamespace(Long clusterId, String namespace, Long configId, Long projectId) {
        return devopsRegistrySecretMapper.baseQueryByClusterIdAndNamespace(
                Objects.requireNonNull(configId),
                Objects.requireNonNull(clusterId),
                Objects.requireNonNull(namespace),
                Objects.requireNonNull(projectId));
    }

    @Override
    public List<DevopsRegistrySecretDTO> baseListByConfig(Long configId) {
        DevopsRegistrySecretDTO devopsRegistrySecretDTO = new DevopsRegistrySecretDTO();
        devopsRegistrySecretDTO.setConfigId(configId);
        return devopsRegistrySecretMapper.select(devopsRegistrySecretDTO);
    }

    @Override
    public DevopsRegistrySecretDTO baseQueryByClusterAndNamespaceAndName(Long clusterId, String namespace, String name) {
        DevopsRegistrySecretDTO devopsRegistrySecretDTO = new DevopsRegistrySecretDTO();
        devopsRegistrySecretDTO.setSecretCode(Objects.requireNonNull(name));
        devopsRegistrySecretDTO.setClusterId(Objects.requireNonNull(clusterId));
        devopsRegistrySecretDTO.setNamespace(Objects.requireNonNull(namespace));
        return devopsRegistrySecretMapper.selectOne(devopsRegistrySecretDTO);
    }

}
