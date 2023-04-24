package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.ClusterCode.DEVOPS_CLUSTER_NOT_EXIST;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.config.CertManagerProperties;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.constant.TimeZoneConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ClientVO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
@Service
public class DevopsClusterResourceServiceImpl implements DevopsClusterResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsClusterResourceServiceImpl.class);

    private static final String GRAFANA_NODE = "/d/choerodon-default-node/jie-dian";
    private static final String GRAFANA_CLUSTER = "/d/choerodon-default-cluster/ji-qun";
    private static final String ERROR_FORMAT = "%s;";
    private static final String GRAFANA_CLIENT_PREFIX = "grafana";
    private static final String DEVOPS_INSERT_CLUSTER_RESOURCE = "devops.insert.cluster.resource";
    private static final String DEVOPS_UPDATE_CLUSTER_RESOURCE = "devops.update.cluster.resource";
    private static final String DEVOPS_CREATE_CERT_MANAGER_EXIST = "devops.create.cert.manager.exist";
    private static final String DEVOPS_INSERT_CERT_MANAGER_RECORD = "devops.insert.cert.manager.record";
    private static final String DEVOPS_INSERT_CERT_MANAGER = "devops.insert.cert.manager";
    private static final String DEVOPS_DELETE_PROMETHEUS = "devops.delete.prometheus";
    private static final String DEVOPS_DELETE_CLUSTER_RESOURCE = "devops.delete.cluster.resource";
    private static final String DEVOPS_NO_CLUSTER_SYSTEM_ENV = "devops.no.cluster.system.env";
    private static final String DEVOPS_INSERT_PROMETHEUS = "devops.insert.prometheus";
    private static final String DEVOPS_CLUSTER_SYSTEM_ENV_ID_NULL = "devops.cluster.system.envId.null";

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
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Lazy
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    private DevopsCertManagerMapper devopsCertManagerMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsPvService devopsPvService;
    @Autowired
    private DevopsEnvFileErrorService devopsEnvFileErrorService;
    @Autowired
    private CertManagerProperties certManagerProperties;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void baseCreate(DevopsClusterResourceDTO devopsClusterResourceDTO) {
        if (devopsClusterResourceMapper.insertSelective(devopsClusterResourceDTO) != 1) {
            throw new CommonException(DEVOPS_INSERT_CLUSTER_RESOURCE);
        }
    }

    @Override
    public void baseUpdate(DevopsClusterResourceDTO devopsClusterResourceDTO) {
        if (devopsClusterResourceMapper.updateByPrimaryKeySelective(devopsClusterResourceDTO) != 1) {
            throw new CommonException(DEVOPS_UPDATE_CLUSTER_RESOURCE);
        }
    }

    @Override
    public void createCertManager(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        DevopsClusterResourceDTO devopsClusterResourceDTO1 = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        if (!ObjectUtils.isEmpty(devopsClusterResourceDTO1)) {
            throw new CommonException(DEVOPS_CREATE_CERT_MANAGER_EXIST);
        }
        DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
        devopsClusterResourceDTO.setType(ClusterResourceType.CERTMANAGER.getType());
        devopsClusterResourceDTO.setClusterId(clusterId);

        DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = new DevopsCertManagerRecordDTO();
        devopsCertManagerRecordDTO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
        MapperUtil.resultJudgedInsertSelective(devopsCertManagerRecordMapper, devopsCertManagerRecordDTO, DEVOPS_INSERT_CERT_MANAGER_RECORD);
        //记录chart信息
        DevopsCertManagerDTO devopsCertManagerDTO = new DevopsCertManagerDTO();
        devopsCertManagerDTO.setReleaseName(certManagerProperties.getReleaseName());
        devopsCertManagerDTO.setNamespace(certManagerProperties.getNamespace());
        devopsCertManagerDTO.setChartVersion(getCertManagerVersion(clusterId));
        MapperUtil.resultJudgedInsertSelective(devopsCertManagerMapper, devopsCertManagerDTO, DEVOPS_INSERT_CERT_MANAGER);
        // 插入数据
        devopsClusterResourceDTO.setObjectId(devopsCertManagerRecordDTO.getId());
        devopsClusterResourceDTO.setClusterId(clusterId);
        devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.INSTALL.getType());
        devopsClusterResourceDTO.setConfigId(devopsCertManagerDTO.getId());
        baseCreate(devopsClusterResourceDTO);
        // 让agent创建cert-manager
        agentCommandService.installCertManager(certManagerProperties.getRepoUrl(), clusterId, devopsCertManagerDTO.getReleaseName(), devopsCertManagerDTO.getNamespace(), devopsCertManagerDTO.getChartVersion());
    }

    @Override
    public void updateCertMangerStatus(Long clusterId, String status, String error) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
        devopsClusterResourceDTO.setType(ClusterResourceType.CERTMANAGER.getType());
        devopsClusterResourceDTO.setClusterId(clusterId);
        DevopsClusterResourceDTO clusterResourceDTO = devopsClusterResourceMapper.selectOne(devopsClusterResourceDTO);

        DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = devopsCertManagerRecordMapper.selectByPrimaryKey(clusterResourceDTO.getObjectId());
        if (!ObjectUtils.isEmpty(status)) {
            devopsCertManagerRecordDTO.setStatus(status);
        }
        devopsCertManagerRecordDTO.setError(error);
        devopsCertManagerRecordMapper.updateByPrimaryKey(devopsCertManagerRecordDTO);
    }

    @Override
    public String queryCertManagerVersion(Long clusterId) {
        return devopsClusterResourceMapper.queryCertManagerVersion(clusterId);
    }

    @Override
    public Boolean deleteCertManager(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        if (checkCertManager(clusterId)) {
            return false;
        }

        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = devopsCertManagerRecordMapper.selectByPrimaryKey(devopsClusterResourceDTO.getObjectId());
        devopsCertManagerRecordDTO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
        devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.UNINSTALL.getType());
        devopsCertManagerRecordMapper.updateByPrimaryKey(devopsCertManagerRecordDTO);
        baseUpdate(devopsClusterResourceDTO);

        // 查询 CertManager 信息
        DevopsCertManagerDTO devopsCertManagerDTO = devopsCertManagerMapper.selectByPrimaryKey(devopsClusterResourceDTO.getConfigId());
        agentCommandService.unloadCertManager(clusterId, devopsCertManagerDTO.getReleaseName(), devopsCertManagerDTO.getNamespace());
        return true;
    }

    @Override
    public Boolean checkCertManager(Long clusterId) {
        List<CertificationDTO> certificationDTOS = devopsCertificationMapper.listClusterCertification(clusterId);
        if (CollectionUtils.isEmpty(certificationDTOS)) {
            return false;
        }
        Set<Long> ids = new HashSet<>();
        for (CertificationDTO dto : certificationDTOS) {
            boolean isFaile = CertificationStatus.FAILED.getStatus().equals(dto.getStatus()) || CertificationStatus.OVERDUE.getStatus().equals(dto.getStatus());
            if (!isFaile) {
                if (CertificationStatus.ACTIVE.getStatus().equals(dto.getStatus()) && !checkValidity(new Date(), dto.getValidFrom(), dto.getValidUntil())) {
                    dto.setStatus(CertificationStatus.OVERDUE.getStatus());
                    CertificationDTO certificationDTO = new CertificationDTO();
                    certificationDTO.setId(dto.getId());
                    certificationDTO.setStatus(CertificationStatus.OVERDUE.getStatus());
                    certificationDTO.setObjectVersionNumber(dto.getObjectVersionNumber());
                    devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
                } else {
                    ids.add(dto.getId());
                    break;
                }
            }
        }
        return !CollectionUtils.isEmpty(ids);
    }


    @Override
    public DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type) {
        return devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, type);
    }

    @Override
    @Transactional
    public List<ClusterResourceVO> listClusterResource(Long clusterId, Long projectId) {
        List<ClusterResourceVO> list = new ArrayList<>();
        // 查询cert-manager 状态
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        ClusterResourceVO clusterConfigVO = new ClusterResourceVO();
        if (ObjectUtils.isEmpty(devopsClusterResourceDTO)) {
            clusterConfigVO.setStatus(ClusterResourceStatus.UNINSTALLED.getStatus());
        } else {
            DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = devopsCertManagerRecordMapper.selectByPrimaryKey(devopsClusterResourceDTO.getObjectId());
            if (!ObjectUtils.isEmpty(devopsCertManagerRecordDTO)) {
                clusterConfigVO.setStatus(devopsCertManagerRecordDTO.getStatus());
                clusterConfigVO.setMessage(devopsCertManagerRecordDTO.getError());
            }
            clusterConfigVO.setOperate(devopsClusterResourceDTO.getOperate());
        }
        clusterConfigVO.setType(ClusterResourceType.CERTMANAGER.getType());
        list.add(clusterConfigVO);
        // 查询prometheus 的状态和信息
        // 屏蔽prometheus组件
//        DevopsClusterResourceDTO prometheus = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
//        ClusterResourceVO clusterResourceVO = new ClusterResourceVO();
//        if (ObjectUtils.isEmpty(prometheus)) {
//            clusterResourceVO.setStatus(ClusterResourceStatus.UNINSTALLED.getStatus());
//        } else {
//            clusterResourceVO = queryPrometheusStatus(projectId, clusterId);
//        }
//        clusterResourceVO.setType(ClusterResourceType.PROMETHEUS.getType());
//        list.add(clusterResourceVO);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unloadCertManager(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        devopsCertManagerRecordMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getObjectId());
        devopsCertManagerMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getConfigId());
        devopsClusterResourceMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getId());
    }

    @Override
    @Transactional
    public void baseDeletePrometheus(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        if (devopsPrometheusMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getConfigId()) != 1) {
            throw new CommonException(DEVOPS_DELETE_PROMETHEUS);
        }
        if (devopsClusterResourceMapper.delete(devopsClusterResourceDTO) != 1) {
            throw new CommonException(DEVOPS_DELETE_CLUSTER_RESOURCE);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createPrometheus(Long projectId, Long clusterId, DevopsPrometheusVO devopsPrometheusVO) {
        DevopsClusterDTO devopsClusterDTO = checkClusterExist(clusterId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        Long systemEnvId = devopsClusterDTO.getSystemEnvId();
        if (systemEnvId == null) {
            throw new CommonException(DEVOPS_NO_CLUSTER_SYSTEM_ENV);
        }
        // 校验环境相关信息

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(systemEnvId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        // 解决分布式事务问题，注册client
        // 集群未注册client，则先注册
        ClientVO clientVO = registerClient(devopsClusterDTO);

        devopsClusterDTO.setClientId(clientVO.getId());
        devopsPrometheusVO.setClientName(clientVO.getName());
        devopsClusterService.baseUpdate(null, devopsClusterDTO);

        DevopsPrometheusDTO newPrometheusDTO = ConvertUtils.convertObject(devopsPrometheusVO, DevopsPrometheusDTO.class);
        DevopsClusterResourceDTO clusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        //安装失败点击安装
        if (clusterResourceDTO != null) {
            retryPrometheusInstance(clusterResourceDTO.getObjectId(), systemEnvId);
        } else {
            //首次点击安装
            newPrometheusDTO.setClusterId(clusterId);
            if (devopsPrometheusMapper.insertSelective(newPrometheusDTO) != 1) {
                throw new CommonException(DEVOPS_INSERT_PROMETHEUS);
            }

            DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
            devopsClusterResourceDTO.setClusterId(clusterId);
            devopsClusterResourceDTO.setConfigId(newPrometheusDTO.getId());
            devopsClusterResourceDTO.setName(devopsClusterDTO.getName());
            devopsClusterResourceDTO.setCode(devopsClusterDTO.getCode());
            devopsClusterResourceDTO.setType(ClusterResourceType.PROMETHEUS.getType());
            devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.INSTALL.getType());
            devopsClusterResourceService.baseCreate(devopsClusterResourceDTO);

            // 安装prometheus
            devopsClusterResourceService.installPrometheus(devopsEnvironmentDTO.getClusterId(), newPrometheusDTO);
        }
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePrometheus(Long projectId, Long clusterId, DevopsPrometheusVO devopsPrometheusVO) {
        DevopsClusterDTO devopsClusterDTO = checkClusterExist(clusterId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        if (devopsClusterDTO.getSystemEnvId() == null) {
            throw new CommonException(DEVOPS_CLUSTER_SYSTEM_ENV_ID_NULL);
        }
        DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(devopsPrometheusVO.getId());
        if (devopsPrometheusDTO.getAdminPassword().equals(devopsPrometheusVO.getAdminPassword())
                && devopsPrometheusDTO.getGrafanaDomain().equals(devopsPrometheusVO.getGrafanaDomain())
                && devopsPrometheusDTO.getEnableTls().equals(devopsPrometheusVO.getEnableTls())) {
            return false;
        }
        // 添加client
        ClientVO clientVO = baseServiceClientOperator.queryClientBySourceId(devopsClusterDTO.getOrganizationId(), devopsClusterDTO.getClientId());
        devopsPrometheusDTO.setClientName(clientVO.getName());

        devopsPrometheusDTO.setAdminPassword(devopsPrometheusVO.getAdminPassword());
        devopsPrometheusDTO.setGrafanaDomain(devopsPrometheusVO.getGrafanaDomain());
        devopsPrometheusDTO.setPrometheusPvId(devopsPrometheusVO.getPrometheusPvId());
        devopsPrometheusDTO.setGrafanaPvId(devopsPrometheusVO.getGrafanaPvId());
        devopsPrometheusDTO.setAlertmanagerPvId(devopsPrometheusVO.getAlertmanagerPvId());
        devopsPrometheusDTO.setEnableTls(devopsPrometheusVO.getEnableTls());
        if (devopsPrometheusMapper.updateByPrimaryKey(devopsPrometheusDTO) != 1) {
            throw new CommonException("devops.prometheus.update");
        }
        // 添加prometheus挂载的pvc
        DevopsClusterResourceDTO clusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        clusterResourceDTO.setOperate(ClusterResourceOperateType.UPGRADE.getType());

        setPvs(devopsPrometheusDTO);
        // 升级prometheus版本
        AppServiceInstanceDTO appServiceInstanceDTO = componentReleaseService.updateReleaseForPrometheus(devopsPrometheusDTO, clusterResourceDTO.getObjectId(), devopsClusterDTO.getSystemEnvId());
        clusterResourceDTO.setObjectId(appServiceInstanceDTO.getId());
        devopsClusterResourceService.baseUpdate(clusterResourceDTO);
        return true;
    }


    @Override
    public DevopsPrometheusVO queryPrometheus(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        DevopsPrometheusVO devopsPrometheusVO = devopsPrometheusMapper.queryPrometheusWithPvById(devopsClusterResourceDTO.getConfigId());

        // 如果PV的状态为null，说明PV已经被删除，则将prometheus绑定的对应PV信息置为null
        // 如果PV的状态是Released，说明PV已经不能使用，也将对应PV信息置为null
        if (devopsPrometheusVO.getGrafanaPvStatus() == null || "Released".equals(devopsPrometheusVO.getGrafanaPvStatus())) {
            devopsPrometheusVO.setGrafanaPvId(null);
            devopsPrometheusVO.setGrafanaPvStatus(null);
            devopsPrometheusVO.setGrafanaPvName(null);
        }

        if (devopsPrometheusVO.getPrometheusPvStatus() == null || "Released".equals(devopsPrometheusVO.getPrometheusPvStatus())) {
            devopsPrometheusVO.setPrometheusPvId(null);
            devopsPrometheusVO.setPrometheusPvStatus(null);
            devopsPrometheusVO.setPrometheusPvName(null);
        }

        if (devopsPrometheusVO.getAlertmanagerPvStatus() == null || "Released".equals(devopsPrometheusVO.getAlertmanagerPvStatus())) {
            devopsPrometheusVO.setAlertmanagerPvId(null);
            devopsPrometheusVO.setAlertmanagerPvStatus(null);
            devopsPrometheusVO.setAlertmanagerPvName(null);

        }
        return devopsPrometheusVO;
    }

    @Override
    public DevopsPrometheusDTO baseQueryPrometheusDTO(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        if (devopsClusterResourceDTO == null) {
            return null;
        } else {
            return devopsPrometheusMapper.selectByPrimaryKey(devopsClusterResourceDTO.getConfigId());
        }
    }

    @Override
    public PrometheusStageVO queryDeployStage(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        PrometheusStageVO prometheusStageVO = new PrometheusStageVO(
                PrometheusDeploy.WAITING.getStaus(),
                PrometheusDeploy.WAITING.getStaus());
        StringBuilder errorStr = new StringBuilder();

        //2.prometheus解析与安装
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsClusterResourceDTO.getObjectId());
        if (appServiceInstanceDTO != null) {
            String parserStatus = getParserStatus(appServiceInstanceDTO.getCommandId(), appServiceInstanceDTO.getEnvId());
            prometheusStageVO.setParserPrometheus(parserStatus);
            if (parserStatus.equals(PrometheusDeploy.SUCCESSED.getStaus())) {
                prometheusStageVO.setInstallPrometheus(PrometheusDeploy.OPERATING.getStaus());
                if (appServiceInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
                    prometheusStageVO.setInstallPrometheus(PrometheusDeploy.SUCCESSED.getStaus());
                } else if (appServiceInstanceDTO.getStatus().equals(InstanceStatus.FAILED.getStatus())) {
                    prometheusStageVO.setInstallPrometheus(PrometheusDeploy.FAILED.getStaus());
                    DevopsEnvCommandDTO prometheusCommand = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
                    errorStr.append(prometheusCommand.getError());
                    //pod异常 安装组件失败，发送webhook
                    sendNotificationService.sendWhenResourceInstallFailed(devopsClusterResourceDTO,
                            SendSettingEnum.RESOURCE_INSTALLFAILED.value(),
                            ClusterResourceType.PROMETHEUS.getType(),
                            clusterId,
                            errorStr.toString());
                }
            } else if (parserStatus.equals(PrometheusDeploy.FAILED.getStaus())) {
                errorStr.append(getErrorDetail(appServiceInstanceDTO.getCommandId(), appServiceInstanceDTO.getEnvId()));
            }
        }
        prometheusStageVO.setError(errorStr.toString());
        return prometheusStageVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uninstallPrometheus(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        componentReleaseService.deleteReleaseForComponent(devopsClusterResourceDTO.getObjectId(), true);
        devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.UNINSTALL.getType());
        devopsClusterResourceService.baseUpdate(devopsClusterResourceDTO);
        return true;
    }

    @Override
    public ClusterResourceVO queryPrometheusStatus(Long projectId, Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        ClusterResourceVO clusterResourceVO = new ClusterResourceVO();
        clusterResourceVO.setType(ClusterResourceType.PROMETHEUS.getType());
        if (devopsClusterResourceDTO == null) {
            clusterResourceVO.setStatus(ClusterResourceStatus.UNINSTALLED.getStatus());
            return clusterResourceVO;
        }
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsClusterResourceDTO.getObjectId());

        //删除操作的状态
        if (ClusterResourceOperateType.UNINSTALL.getType().equals(devopsClusterResourceDTO.getOperate())) {
            //查看promtheus对应的实例是否存在，不存在即为已经删除
            if (appServiceInstanceDTO == null) {
                DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(devopsClusterResourceDTO.getConfigId());
                if (devopsPrometheusDTO == null) {
                    clusterResourceVO.setStatus(ClusterResourceStatus.UNINSTALLED.getStatus());
                    return clusterResourceVO;
                }
            }
            clusterResourceVO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
            clusterResourceVO.setOperate(devopsClusterResourceDTO.getOperate());
            return clusterResourceVO;
        }

        //升级和安装操作的状态
        PrometheusStageVO prometheusStageVO = queryDeployStage(clusterId);
        if (prometheusStageVO.getParserPrometheus().equals(PrometheusDeploy.FAILED.getStaus())
                || prometheusStageVO.getInstallPrometheus().equals(PrometheusDeploy.FAILED.getStaus())) {
            if (ClusterResourceOperateType.UPGRADE.getType().equals(devopsClusterResourceDTO.getOperate())) {
                checkPodIsReady(appServiceInstanceDTO.getId(), clusterResourceVO);
            } else {
                clusterResourceVO.setStatus(ClusterResourceStatus.UNINSTALLED.getStatus());
            }
            clusterResourceVO.setMessage(prometheusStageVO.getError());
        } else if (prometheusStageVO.getInstallPrometheus().equals(PrometheusDeploy.SUCCESSED.getStaus())) {
            checkPodIsReady(appServiceInstanceDTO.getId(), clusterResourceVO);
        } else {
            clusterResourceVO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
        }
        clusterResourceVO.setOperate(devopsClusterResourceDTO.getOperate());
        return clusterResourceVO;
    }

    @Override
    public String getGrafanaUrl(Long projectId, Long clusterId, String type) {
        DevopsClusterResourceDTO clusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        ClusterResourceVO clusterResourceVO = queryPrometheusStatus(projectId, clusterId);
        if (!clusterResourceVO.getStatus().equals(ClusterResourceStatus.AVAILABLE.getStatus())) {
            return null;
        }
        DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(clusterResourceDTO.getConfigId());
        String grafanaType = type.equals("node") ? GRAFANA_NODE : GRAFANA_CLUSTER;
        if (Boolean.TRUE.equals(devopsPrometheusDTO.getEnableTls())) {
            return String.format("%s%s%s", "https://", devopsPrometheusDTO.getGrafanaDomain(), grafanaType);
        } else {
            return String.format("%s%s%s", "http://", devopsPrometheusDTO.getGrafanaDomain(), grafanaType);
        }
    }

    @Override
    public void installPrometheus(Long clusterId, DevopsPrometheusDTO devopsPrometheusDTO) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        DevopsClusterResourceDTO clusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        // 设置当前操作用户
        CustomContextUtil.setUserContext(clusterResourceDTO.getLastUpdatedBy());
        devopsPrometheusDTO.setClusterCode(devopsClusterDTO.getCode());
        setPvs(devopsPrometheusDTO);
        AppServiceInstanceDTO appServiceInstanceDTO = componentReleaseService.createReleaseForPrometheus(devopsClusterDTO.getSystemEnvId(), devopsPrometheusDTO);
        clusterResourceDTO.setObjectId(appServiceInstanceDTO.getId());
        devopsClusterResourceService.baseUpdate(clusterResourceDTO);
    }

    private void setPvs(DevopsPrometheusDTO devopsPrometheusDTO) {
        devopsPrometheusDTO.setGrafanaPv(devopsPvService.baseQueryById(devopsPrometheusDTO.getGrafanaPvId()));
        devopsPrometheusDTO.setAltermanagerPv(devopsPvService.baseQueryById(devopsPrometheusDTO.getAlertmanagerPvId()));
        devopsPrometheusDTO.setPrometheusPv(devopsPvService.baseQueryById(devopsPrometheusDTO.getPrometheusPvId()));
    }

    @Override
    public Boolean queryCertManagerByEnvId(Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(devopsEnvironmentDTO.getClusterId(), ClusterResourceType.CERTMANAGER.getType());
        return !ObjectUtils.isEmpty(devopsClusterResourceDTO);
    }


    @Override
    public void retryPrometheusInstance(Long instanceId, Long envId) {
        // 三步重试每次只重试一步
        if (componentReleaseService.retryPushingToGitLab(instanceId, ClusterResourceType.PROMETHEUS)) {
            return;
        }

        if (devopsEnvironmentService.retrySystemEnvGitOps(envId)) {
            return;
        }

        if (componentReleaseService.restartComponentInstance(instanceId, ClusterResourceType.PROMETHEUS)) {
            return;
        }
    }

    @Override
    public void retryInstallPrometheus(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        CommonExAssertUtil.assertTrue(projectId.equals(devopsClusterDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        DevopsClusterResourceDTO clusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        if (clusterResourceDTO == null || clusterResourceDTO.getObjectId() == null) {
            throw new CommonException("devops.prometheus.retry");
        } else {
            AppServiceInstanceDTO instanceDTO = appServiceInstanceService.baseQuery(clusterResourceDTO.getObjectId());
            retryPrometheusInstance(clusterResourceDTO.getObjectId(), instanceDTO.getEnvId());
        }
    }

    private void checkPodIsReady(Long instanceId, ClusterResourceVO clusterResourceVO) {
        List<DevopsEnvPodVO> devopsEnvPodDTOS = ConvertUtils.convertList(devopsEnvPodService.baseListByInstanceId(instanceId), DevopsEnvPodVO.class);
        //查询pod状态
        devopsEnvPodDTOS.forEach(devopsEnvPodVO -> devopsEnvPodService.fillContainers(devopsEnvPodVO));
        List<ContainerVO> readyContainers = new ArrayList<>();
        Integer totalNum = 0;
        //健康检查，ready=true的pod大于1就是可用的
        for (DevopsEnvPodVO devopsEnvPodVO : devopsEnvPodDTOS) {
            if (Boolean.TRUE.equals(devopsEnvPodVO.getReady()) && devopsEnvPodVO.getContainers() != null) {
                readyContainers.addAll(devopsEnvPodVO.getContainers().stream().filter(ContainerVO::getReady).collect(Collectors.toList()));
                totalNum = totalNum + devopsEnvPodVO.getContainers().size();
            } else {
                clusterResourceVO.setStatus(ClusterResourceStatus.DISABLED.getStatus());
                return;
            }
        }
        if (!readyContainers.isEmpty() && readyContainers.size() == totalNum) {
            clusterResourceVO.setStatus(ClusterResourceStatus.AVAILABLE.getStatus());
        } else {
            clusterResourceVO.setStatus(ClusterResourceStatus.DISABLED.getStatus());
        }
    }


    private StringBuilder getErrorDetail(Long commandId, Long envId) {
        StringBuilder errorStr = new StringBuilder();
        DevopsEnvCommandDTO prometheusCommand = devopsEnvCommandService.baseQuery(commandId);
        if (prometheusCommand.getStatus().equals(CommandStatus.FAILED.getStatus())) {
            errorStr.append(prometheusCommand.getError());
        } else {
            List<DevopsEnvFileErrorDTO> fileErrorDTOList = devopsEnvFileErrorService.baseListByEnvId(envId);
            fileErrorDTOList.forEach(fileError -> {
                errorStr.append("error:");
                errorStr.append(String.format(ERROR_FORMAT, fileError.getFilePath()));
                errorStr.append(String.format(ERROR_FORMAT, fileError.getError()));
            });
        }
        return errorStr;
    }

    /**
     * 获取pvc和prometheus 解析结果
     *
     * @param commandId commandId
     * @param envId     环境id
     * @return 状态
     */
    private String getParserStatus(Long commandId, Long envId) {
        DevopsEnvCommandDTO prometheusCommand = devopsEnvCommandService.baseQuery(commandId);
        if (prometheusCommand.getSha() != null) {
            return PrometheusDeploy.SUCCESSED.getStaus();
        } else {
            List<DevopsEnvFileErrorDTO> fileErrorDTOList = devopsEnvFileErrorService.baseListByEnvId(envId);
            if (CommandStatus.OPERATING.getStatus().equals(prometheusCommand.getStatus())
                    && (fileErrorDTOList == null || fileErrorDTOList.isEmpty())) {
                return PrometheusDeploy.OPERATING.getStaus();
            } else {
                return PrometheusDeploy.FAILED.getStaus();
            }
        }
    }

    private Boolean checkValidity(Date date, Date validFrom, Date validUntil) {
        return validFrom != null && validUntil != null
                && date.after(validFrom) && date.before(validUntil);
    }

    protected DevopsClusterDTO checkClusterExist(Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO == null) {
            throw new CommonException(DEVOPS_CLUSTER_NOT_EXIST);
        }
        return devopsClusterDTO;
    }

    protected ClientVO registerClient(DevopsClusterDTO devopsClusterDTO) {

        String clientName = GRAFANA_CLIENT_PREFIX + UUID.randomUUID().toString().substring(0, 6);
        // 添加客户端
        ClientVO clientVO = baseServiceClientOperator.queryClientByName(devopsClusterDTO.getOrganizationId(), clientName);
        if (clientVO != null && clientVO.getId() != null) {
            return clientVO;
        }

        clientVO = new ClientVO();
        clientVO.setName(clientName);
        clientVO.setOrganizationId(devopsClusterDTO.getOrganizationId());
        clientVO.setAuthorizedGrantTypes("password,implicit,client_credentials,refresh_token,authorization_code");
        clientVO.setSecret("grafana");
        clientVO.setRefreshTokenValidity(360000L);
        clientVO.setAccessTokenValidity(360000L);
        clientVO.setPwdReplayFlag(0);
        clientVO.setTimeZone(TimeZoneConstants.GMT8);
        clientVO.setSourceId(devopsClusterDTO.getId());
        clientVO.setSourceType("cluster");
        LOGGER.info("clientVO:{}", clientVO);
        // 获取当前组织下的项目管理员的角色id
        Long projectAdminId = baseServiceClientOperator.getRoleId(devopsClusterDTO.getOrganizationId(), RoleLabel.PROJECT_ADMIN.getValue(), LabelType.PROJECT_ADMIN.getValue());
        // 获取当前组织管理员的角色id
        Long tenantAdminId = baseServiceClientOperator.getRoleId(devopsClusterDTO.getOrganizationId(), RoleLabel.TENANT_ADMIN.getValue(), LabelType.TENANT_ADMIN.getValue());
        // 添加两个角色id
        clientVO.setAccessRoles(String.valueOf(projectAdminId).concat(",").concat(String.valueOf(tenantAdminId)));
        return baseServiceClientOperator.createClient(devopsClusterDTO.getOrganizationId(), clientVO);
    }

    public String getCertManagerVersion(Long clusterId) {
        ClusterSummaryInfoVO clusterSummaryInfoVO = JsonHelper.unmarshalByJackson(redisTemplate.opsForValue().get(DevopsClusterServiceImpl.renderClusterInfoRedisKey(clusterId)), ClusterSummaryInfoVO.class);
        String[] split = clusterSummaryInfoVO.getVersion().split("\\.");
        int minorVersion = Integer.parseInt(split[1]);
        if (minorVersion <= 21) {
            return "v1.1.1";
        } else {
            return "v1.8.2";
        }
    }
}
