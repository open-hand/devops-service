package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsRegistrySecretServiceImpl.class);

    @Autowired
    private DevopsRegistrySecretMapper devopsRegistrySecretMapper;

    @Override
    public void deleteByEnvId(Long envId) {
        DevopsRegistrySecretDTO deleteCondition = new DevopsRegistrySecretDTO();
        deleteCondition.setEnvId(Objects.requireNonNull(envId));
        devopsRegistrySecretMapper.delete(deleteCondition);
    }

    @Override
    public DevopsRegistrySecretDTO baseCreate(DevopsRegistrySecretDTO devopsRegistrySecretDTO) {
        if (devopsRegistrySecretMapper.insert(devopsRegistrySecretDTO) != 1) {
            throw new CommonException("error.registry.secret.create.error");
        }
        return devopsRegistrySecretDTO;
    }

    /**
     * 这里的事务隔离级别要是 Propagation.NOT_SUPPORTED
     * 因为agent会回写secret的状态数据，但是假如事务的隔离级别
     * 是REQUIRED，如果创建实例的事务没提交，agent返回的数据查不到
     * 这条未提交的数据，就更新不到了
     * 而且这个secret也不需要回滚
     *
     * @param devopsRegistrySecretDTO secret数据
     * @return 相应的secret数据
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DevopsRegistrySecretDTO createIfNonInDb(DevopsRegistrySecretDTO devopsRegistrySecretDTO) {
        DevopsRegistrySecretDTO dbResult = baseQueryByClusterAndNamespaceAndName(devopsRegistrySecretDTO.getClusterId(), devopsRegistrySecretDTO.getNamespace(), devopsRegistrySecretDTO.getSecretCode());
        LOGGER.debug("Registry secret: createIfNonInDb: the dbResult is {}", dbResult);
        // 如果不存在才创建
        return dbResult == null ? baseCreate(devopsRegistrySecretDTO) : dbResult;
    }

    @Override
    public DevopsRegistrySecretDTO baseQuery(Long devopsRegistrySecretId) {
        return devopsRegistrySecretMapper.selectByPrimaryKey(devopsRegistrySecretId);
    }

    @Transactional
    @Override
    public DevopsRegistrySecretDTO baseUpdate(DevopsRegistrySecretDTO devopsRegistrySecretDTO) {
        DevopsRegistrySecretDTO beforeDevopsRegistrySecretDTO = devopsRegistrySecretMapper.selectByPrimaryKey(devopsRegistrySecretDTO.getId());
        devopsRegistrySecretDTO.setObjectVersionNumber(beforeDevopsRegistrySecretDTO.getObjectVersionNumber());
        if (devopsRegistrySecretMapper.updateByPrimaryKeySelective(devopsRegistrySecretDTO) != 1) {
            throw new CommonException("error.registry.secret.update.error");
        }
        return beforeDevopsRegistrySecretDTO;
    }

    @Transactional
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
