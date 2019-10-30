package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.app.service.DevopsClusterResourceService;
import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;
import io.choerodon.devops.infra.enums.CertificationStatus;
import io.choerodon.devops.infra.enums.ClusterResourceStatus;
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper;
import io.choerodon.devops.infra.mapper.DevopsClusterResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;

    @Override
    public void baseCreate(DevopsClusterResourceDTO devopsClusterResourceDTO) {
        if (devopsClusterResourceMapper.insertSelective(devopsClusterResourceDTO) != 1) {
            throw new CommonException("error.insert.cluster.resource");
        }
    }

    public void baseUpdate(DevopsClusterResourceDTO devopsClusterResourceDTO) {
        if (devopsClusterResourceMapper.updateByPrimaryKeySelective(devopsClusterResourceDTO) != 1) {
            throw new CommonException("error.update.cluster.resource");
        }

    }

    @Override
    public void operateCertManager(DevopsClusterResourceDTO devopsClusterResourceDTO, Long clusterId) {
        DevopsClusterResourceDTO clusterResourceDTO = devopsClusterResourceMapper.queryByOptions(devopsClusterResourceDTO);
        if (!ObjectUtils.isEmpty(clusterResourceDTO)) {
            baseUpdate(clusterResourceDTO);
        } else {
            // 新增集群的資源
            devopsClusterResourceDTO.setClusterId(clusterId);
            baseCreate(devopsClusterResourceDTO);
            // 让agent创建cert-mannager
            agentCommandService.createCertManager(clusterId);
            // 发送消息告知agent,发送创建cert-manager的信息
            // agentCommandService.getCertManagerStatus(clusterId);
        }
    }
    @Override
    public DevopsClusterResourceDTO queryCertManager(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
        devopsClusterResourceDTO.setClusterId(clusterId);
        devopsClusterResourceDTO.setType("cert-mannager");
        DevopsClusterResourceDTO devopsClusterResourceDTO1 = devopsClusterResourceMapper.selectOne(devopsClusterResourceDTO);
        return devopsClusterResourceDTO1;
    }

    @Override
    public Boolean deleteCertManager(Long clusterId) {
        if (!checkCertManager(clusterId)) {
            return false;
        }
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, "cert-mannager");
        baseUpdate(devopsClusterResourceDTO);
        agentCommandService.unloadCertManager(clusterId);
        return true;
    }

    public Boolean checkValidity(Date date, Date validFrom, Date validUntil) {
        return validFrom != null && validUntil != null
                && date.after(validFrom) && date.before(validUntil);
    }

    /**
     * 验证cert-manager 管理的证书是否存在启用或者操作状态的
     *
     * @param clusterId
     * @return
     */
    @Override
    public  Boolean checkCertManager(Long clusterId) {
        List<CertificationDTO> certificationDTOS = devopsCertificationMapper.listClusterCertification(clusterId);
        if (CollectionUtils.isEmpty(certificationDTOS)) {
            return false;
        }
        Set<Long> ids = new HashSet<>();
        certificationDTOS.forEach(dto -> {
            boolean is_faile = CertificationStatus.FAILED.getStatus().equals(dto.getStatus()) || CertificationStatus.OVERDUE.getStatus().equals(dto.getStatus());
            if (!is_faile) {
                if (CertificationStatus.ACTIVE.getStatus().equals(dto.getStatus())) {
                    if (!checkValidity(new Date(), dto.getValidFrom(), dto.getValidUntil())) {
                        dto.setStatus(CertificationStatus.OVERDUE.getStatus());
                        CertificationDTO certificationDTO = new CertificationDTO();
                        certificationDTO.setId(dto.getId());
                        certificationDTO.setStatus(CertificationStatus.OVERDUE.getStatus());
                        certificationDTO.setObjectVersionNumber(dto.getObjectVersionNumber());
                        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
                    }
                } else {
                    ids.add(dto.getId());
                }
            }
        });
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        return false;
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

    @Override
    public List<DevopsClusterResourceDTO> listClusterResource(Long clusterId) {
        return null;
    }
}
