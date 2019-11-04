package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ClusterResourceVO;
import io.choerodon.devops.api.vo.ContainerVO;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.api.vo.DevopsPrometheusVO;
import io.choerodon.devops.app.eventhandler.constants.CertManagerConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ClientDTO;
import io.choerodon.devops.infra.dto.iam.ClientVO;
import io.choerodon.devops.infra.enums.CertificationStatus;
import io.choerodon.devops.infra.enums.ClusterResourceOperateType;
import io.choerodon.devops.infra.enums.ClusterResourceStatus;
import io.choerodon.devops.infra.enums.ClusterResourceType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GenerateUUID;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
@Service
public class DevopsClusterResourceServiceImpl implements DevopsClusterResourceService {
    private static final String PROMETHEUS_PREFIX = "prometheus-";

    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_FAIL = "fail";
    private static final String STATUS_CREATED = "created";
    private static final String STATUS_SUCCESSED = "success";
    private static final String STATUS_CHECK_FAIL = "check_fail";
    private static final String GRAFANA_NODE = "/d/choerodon-default-node/jie-dian";
    private static final String GRAFANA_CLUSTER = "/d/choerodon-default-cluster/ji-qun";
    @Autowired
    private DevopsClusterResourceMapper devopsClusterResourceMapper;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsCertManagerRecordMapper devopsCertManagerRecordMapper;
    @Autowired
    private DevopsPrometheusMapper devopsPrometheusMapper;
    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;

    @Autowired
    private ComponentReleaseService componentReleaseService;

    @Autowired
    private AppServiceInstanceService appServiceInstanceService;

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;

    @Autowired
    private UserAttrService userAttrService;

    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;

    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;

    @Autowired
    private DevopsClusterService devopsClusterService;

    @Autowired
    private DevopsEnvPodService devopsEnvPodService;

    @Autowired
    private DevopsCertManagerMapper devopsCertManagerMapper;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

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
    public void operateCertManager(Long clusterId, String status, String error) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
        devopsClusterResourceDTO.setType(ClusterResourceType.CERTMANAGER.getType());
        devopsClusterResourceDTO.setClusterId(clusterId);
        DevopsClusterResourceDTO clusterResourceDTO = devopsClusterResourceMapper.queryByOptions(devopsClusterResourceDTO);
        if (!ObjectUtils.isEmpty(clusterResourceDTO)) {
            DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = devopsCertManagerRecordMapper.selectByPrimaryKey(clusterResourceDTO.getObjectId());
            devopsCertManagerRecordDTO.setError(error);
            devopsCertManagerRecordDTO.setStatus(status);
            devopsCertManagerRecordMapper.updateByPrimaryKeySelective(devopsCertManagerRecordDTO);
        } else {
            DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = new DevopsCertManagerRecordDTO();
            devopsCertManagerRecordDTO.setStatus(status);
            devopsCertManagerRecordDTO.setError(error);
            if (ObjectUtils.isEmpty(status)) {
                devopsCertManagerRecordDTO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
            }
            devopsCertManagerRecordMapper.insertSelective(devopsCertManagerRecordDTO);
            //记录chart信息
            DevopsCertManagerDTO devopsCertManagerDTO = new DevopsCertManagerDTO();
            devopsCertManagerDTO.setNamespace(CertManagerConstants.CERT_MANAGER_NAME_SPACE);
            devopsCertManagerDTO.setChartVersion(CertManagerConstants.CERT_MANAGER_CHART_VERSION);
            devopsCertManagerMapper.insertSelective(devopsCertManagerDTO);
            // 插入数据
            devopsClusterResourceDTO.setObjectId(devopsCertManagerRecordDTO.getId());
            devopsClusterResourceDTO.setClusterId(clusterId);
            devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.INSTALL.getType());
            devopsClusterResourceDTO.setConfigId(devopsCertManagerDTO.getId());
            baseCreate(devopsClusterResourceDTO);
            // 让agent创建cert-mannager
            agentCommandService.createCertManager(clusterId);
        }
    }

    @Override
    public Boolean deleteCertManager(Long clusterId) {
        if (!checkCertManager(clusterId)) {
            return false;
        }
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = devopsCertManagerRecordMapper.selectByPrimaryKey(devopsClusterResourceDTO.getObjectId());
        devopsCertManagerRecordDTO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
        devopsCertManagerRecordMapper.updateByPrimaryKey(devopsCertManagerRecordDTO);
        agentCommandService.unloadCertManager(clusterId);
        devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.UNINSTALL.getType());
        baseUpdate(devopsClusterResourceDTO);
        return true;
    }

    public Boolean checkValidity(Date date, Date validFrom, Date validUntil) {
        return validFrom != null && validUntil != null
                && date.after(validFrom) && date.before(validUntil);
    }

    @Override
    public Boolean checkCertManager(Long clusterId) {
        List<CertificationDTO> certificationDTOS = devopsCertificationMapper.listClusterCertification(clusterId);
        if (CollectionUtils.isEmpty(certificationDTOS)) {
            return true;
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
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndTypeAndConfigId(clusterId, ClusterResourceType.PROMETHEUS.getType(), configId);
        return devopsClusterResourceDTO;
    }


    @Override
    public DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, type);
        return devopsClusterResourceDTO;
    }

    @Override
    public List<ClusterResourceVO> listClusterResource(Long clusterId, Long projectId) {
        List<ClusterResourceVO> list = new ArrayList<>();
        // 查询cert-manager 状态
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        ClusterResourceVO clusterConfigVO = new ClusterResourceVO();
        if (ObjectUtils.isEmpty(devopsClusterResourceDTO)) {
            clusterConfigVO.setStatus(ClusterResourceStatus.UNINSTALL.getStatus());
        } else {
            DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = devopsCertManagerRecordMapper.selectByPrimaryKey(devopsClusterResourceDTO.getObjectId());
            if (!ObjectUtils.isEmpty(devopsCertManagerRecordDTO)) {
                clusterConfigVO.setStatus(devopsCertManagerRecordDTO.getStatus());
                clusterConfigVO.setMessage(devopsCertManagerRecordDTO.getError());
            }
            clusterConfigVO.setType(ClusterResourceType.CERTMANAGER.getType());
        }
        clusterConfigVO.setOperate(devopsClusterResourceDTO.getOperate());
        list.add(clusterConfigVO);
        // 查询prometheus 的状态和信息
        DevopsClusterResourceDTO prometheus = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        ClusterResourceVO clusterResourceVO = new ClusterResourceVO();
        if(ObjectUtils.isEmpty(prometheus)){
            clusterResourceVO.setStatus(ClusterResourceStatus.UNINSTALL.getStatus());
        }
        else {
            Long configId = prometheus.getConfigId();
            clusterResourceVO = queryPrometheusStatus(projectId, clusterId);
        }
        clusterResourceVO.setType(ClusterResourceType.PROMETHEUS.getType());
        list.add(clusterResourceVO);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unloadCertManager(Long clusterId) {
        List<CertificationDTO> certificationDTOS = devopsCertificationMapper.listClusterCertification(clusterId);
        if (!CollectionUtils.isEmpty(certificationDTOS)) {
            certificationDTOS.forEach(v -> {
                devopsCertificationMapper.deleteByPrimaryKey(v.getId());
            });
        }
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        devopsCertManagerRecordMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getObjectId());
        devopsCertManagerMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getConfigId());
        devopsClusterResourceMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getId());
    }

    @Override
    @Transactional
    public void basedeletePromtheus(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        if (devopsPrometheusMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getConfigId()) != 1) {
            throw new CommonException("error.delete.devopsPrometheus");
        }
        if (devopsClusterResourceMapper.delete(devopsClusterResourceDTO) != 1) {
            throw new CommonException("error.delete.cluster.resource");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdate(Long clusterId, DevopsPrometheusVO devopsPrometheusVO) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO.getSystemEnvId() == null) {
            throw new CommonException("no.cluster.SystemEnvId");
        }
        DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
        DevopsPrometheusDTO devopsPrometheusDTO = prometheusVoToDto(devopsPrometheusVO);
        if (ObjectUtils.isEmpty(devopsPrometheusVO.getId())) {
            devopsPrometheusDTO.setClusterId(clusterId);
            if (devopsPrometheusMapper.insertSelective(devopsPrometheusDTO) != 1) {
                throw new CommonException("error.inster.prometheus");
            }
            devopsPrometheusDTO.setId(devopsPrometheusDTO.getId());
            if (ObjectUtils.isEmpty(devopsClusterDTO.getClientId())) {
                // 添加客户端
                ClientVO clientVO = new ClientVO();
                clientVO.setName(devopsClusterDTO.getChoerodonId());
                clientVO.setOrganizationId(devopsClusterDTO.getOrganizationId());
                clientVO.setAuthorizedGrantTypes("password,implicit,client_credentials,refresh_token,authorization_code");
                clientVO.setSecret(GenerateUUID.generateUUID().substring(0, 16));
                clientVO.setRefreshTokenValidity(3600L);
                clientVO.setAccessTokenValidity(3600L);
                clientVO.setSourceId(clusterId);
                clientVO.setSourceType("cluster");
                ClientDTO client = baseServiceClientOperator.createClient(devopsClusterDTO.getOrganizationId(), clientVO);
                if (!ObjectUtils.isEmpty(client)) {
                    devopsClusterDTO.setClientId(client.getId());
                    devopsClusterService.baseUpdate(devopsClusterDTO);
                }
            }
            DevopsClusterResourceDTO devopsClusterResource = devopsClusterResourceService.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
            if(devopsClusterResource!=null){
                throw new CommonException("prometheus already exist");
            }
            AppServiceInstanceDTO appServiceInstanceDTO = componentReleaseService.createReleaseForPrometheus(devopsClusterDTO.getSystemEnvId(), devopsPrometheusDTO);
            devopsClusterResourceDTO.setClusterId(clusterId);
            devopsClusterResourceDTO.setConfigId(devopsPrometheusDTO.getId());
            devopsClusterResourceDTO.setObjectId(appServiceInstanceDTO.getId());
            devopsClusterResourceDTO.setName(devopsClusterDTO.getName());
            devopsClusterResourceDTO.setCode(devopsClusterDTO.getCode());
            devopsClusterResourceDTO.setType(ClusterResourceType.PROMETHEUS.getType());
            devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.INSTALL.getType());
            devopsClusterResourceService.baseCreate(devopsClusterResourceDTO);
        } else {
            DevopsClusterResourceDTO devopsClusterResource = devopsClusterResourceService.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
            AppServiceInstanceDTO appServiceInstanceDTO = componentReleaseService.updateReleaseForPrometheus(devopsPrometheusDTO, devopsClusterResource.getObjectId(), devopsClusterDTO.getSystemEnvId());
            devopsClusterResourceDTO.setObjectId(appServiceInstanceDTO.getId());
            if (devopsPrometheusMapper.updateByPrimaryKey(devopsPrometheusDTO) != 1) {
                throw new CommonException("error.update.prometheus");
            }
            devopsClusterResourceDTO.setObjectId(appServiceInstanceDTO.getId());
            devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.UPGRADE.getType());
            devopsClusterResourceService.baseUpdate(devopsClusterResourceDTO);

        }

    }

    @Override
    public DevopsPrometheusVO queryPrometheus(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(devopsClusterResourceDTO.getConfigId());
        DevopsPrometheusVO devopsPrometheusVO = ConvertUtils.convertObject(devopsPrometheusDTO, DevopsPrometheusVO.class);
        return devopsPrometheusVO;
    }

    @Override
    public ClusterResourceVO queryDeployProcess(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsClusterResourceDTO.getObjectId());
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());

        ClusterResourceVO clusterResourceVO = new ClusterResourceVO();
        clusterResourceVO.setType(ClusterResourceType.PROMETHEUS.getType());
        if (!ObjectUtils.isEmpty(devopsEnvCommandDTO.getSha())) {
            clusterResourceVO.setStatus(STATUS_CREATED);
        }
        if (appServiceInstanceDTO.getStatus().equals(STATUS_RUNNING)) {
            clusterResourceVO.setStatus(STATUS_RUNNING);
        } else {
            clusterResourceVO.setMessage(devopsEnvCommandDTO.getError());
            clusterResourceVO.setStatus(STATUS_FAIL);
        }
        return clusterResourceVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadPrometheus(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        componentReleaseService.deleteReleaseForComponent(devopsClusterResourceDTO.getObjectId(), true);
        devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.UNINSTALL.getType());
        devopsClusterResourceService.baseUpdate(devopsClusterResourceDTO);
    }

    @Override
    public ClusterResourceVO queryPrometheusStatus(Long projectId, Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        ClusterResourceVO clusterResourceVO = new ClusterResourceVO();
        clusterResourceVO.setType(ClusterResourceType.PROMETHEUS.getType());
        if (devopsClusterResourceDTO == null) {
            clusterResourceVO.setStatus(ClusterResourceStatus.UNINSTALL.getStatus());
            return clusterResourceVO;
        }
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsClusterResourceDTO.getObjectId());
        //删除操作的状态
        if (ClusterResourceOperateType.UNINSTALL.getType().equals(devopsClusterResourceDTO.getOperate())) {
            //查看promtheus对应的实例是否存在，不存在即为已经删除，再删除promtheus
            if (appServiceInstanceDTO == null) {
                basedeletePromtheus(clusterId);
                clusterResourceVO.setStatus(ClusterResourceStatus.UNINSTALL.getStatus());
                return clusterResourceVO;
            }
            clusterResourceVO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
            clusterResourceVO.setOperate(devopsClusterResourceDTO.getOperate());
            return clusterResourceVO;
        }
        //升级和安装操作的状态
        clusterResourceVO = queryDeployProcess(clusterId);
        List<DevopsEnvPodVO> devopsEnvPodDTOS = ConvertUtils.convertList(devopsEnvPodService.baseListByInstanceId(appServiceInstanceDTO.getId()), DevopsEnvPodVO.class);
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
        if (STATUS_CREATED.equals(clusterResourceVO.getStatus())) {
            clusterResourceVO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
        }

        if (STATUS_RUNNING.equals(clusterResourceVO.getStatus())) {
            clusterResourceVO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
            //查询pod状态
            devopsEnvPodDTOS.stream().forEach(devopsEnvPodVO -> {
                devopsEnvPodService.fillContainers(devopsEnvPodVO);
            });

            List<ContainerVO> readyPod = new ArrayList<>();
            //ready=true的pod大于1就是可用的
            devopsEnvPodDTOS.stream().forEach(devopsEnvPodVO -> {
                if (devopsEnvPodVO.getReady() == true) {
                    readyPod.addAll(devopsEnvPodVO.getContainers().stream().filter(pod -> pod.getReady() == true).collect(Collectors.toList()));
                }
            });
            if (readyPod.size() >= 1) {
                clusterResourceVO.setStatus(STATUS_SUCCESSED);
            } else {
                clusterResourceVO.setStatus(ClusterResourceStatus.DISABLED.getStatus());
                clusterResourceVO.setMessage(devopsEnvCommandDTO.getError());
            }
        }

        clusterResourceVO.setOperate(devopsClusterResourceDTO.getOperate());
        return clusterResourceVO;
    }

    private DevopsPrometheusDTO prometheusVoToDto(DevopsPrometheusVO prometheusVo) {
        DevopsPrometheusDTO devopsPrometheusDTO = new DevopsPrometheusDTO();
        BeanUtils.copyProperties(prometheusVo, devopsPrometheusDTO);
        return devopsPrometheusDTO;
    }

    @Override
    public String getGrafanaUrl(Long projectId, Long clusterId, String type) {
        DevopsClusterResourceDTO clusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        if (clusterResourceDTO == null) {
            return null;
        }
        DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(clusterResourceDTO.getConfigId());
        String grafanaType = type.equals("node") ? GRAFANA_NODE : GRAFANA_CLUSTER;
        return String.format("%s%s%s", "http://", devopsPrometheusDTO.getGrafanaDomain(), grafanaType);
    }

}
