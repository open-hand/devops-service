package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.app.service.DevopsClusterResourceService;
import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;
import io.choerodon.devops.infra.mapper.DevopsClusterResourceMapper;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
@Service
public class DevopsClusterResourceServiceImpl implements DevopsClusterResourceService {
    @Autowired
    private DevopsClusterResourceMapper devopsClusterResourceMapper;
    @Autowired
    private AgentCommandService agentCommandService;

    @Override
    public void baseCreateOrUpdate(DevopsClusterResourceDTO devopsClusterResourceDTO) {
        DevopsClusterResourceDTO devopsClusterResourceDTO1 = devopsClusterResourceMapper.queryByClusterIdAndType(devopsClusterResourceDTO.getClusterId(), devopsClusterResourceDTO.getType());
        if (!ObjectUtils.isEmpty(devopsClusterResourceDTO1)) {
            devopsClusterResourceDTO.setId(devopsClusterResourceDTO1.getId());
            devopsClusterResourceDTO.setObjectVersionNumber(devopsClusterResourceDTO1.getObjectVersionNumber());
            devopsClusterResourceMapper.updateByPrimaryKey(devopsClusterResourceDTO);
            return;
        }
        devopsClusterResourceMapper.insertSelective(devopsClusterResourceDTO);
    }

    @Override
    public void create(DevopsClusterResourceDTO devopsClusterResourceDTO, Long clusterId) {
        if (ObjectUtils.isEmpty(devopsClusterResourceDTO)) {
            throw new CommonException("cluster.resource.is.not.null");
        }
        // 新增或者修改集群的資源
        devopsClusterResourceDTO.setClusterId(clusterId);
        baseCreateOrUpdate(devopsClusterResourceDTO);
        if ("cert-manager".equals(devopsClusterResourceDTO.getType())) {
            // 让agent创建cert-mannager
            agentCommandService.createCertManager(clusterId);
            // 发送消息告知agent,发送创建cert-manager的信息
//            agentCommandService.getCertManagerStatus(clusterId);
        }
    }

    @Override
    public DevopsClusterResourceDTO queryByClusterIdAndConfigId(Long clusterId, Long configId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndConfigId(clusterId, configId);
        return devopsClusterResourceDTO;
    }


    @Override
    public DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, type);
        return devopsClusterResourceDTO;
    }

    @Override
    public void delete(Long clusterId, Long configId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
        devopsClusterResourceDTO.setConfigId(configId);
        devopsClusterResourceDTO.setClusterId(clusterId);
        devopsClusterResourceMapper.delete(devopsClusterResourceDTO);
    }
}
