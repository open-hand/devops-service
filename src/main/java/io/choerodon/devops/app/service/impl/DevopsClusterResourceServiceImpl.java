package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.constants.CertManagerConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ClientDTO;
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
    private static final String ERROR_BOUND_PVC_FORMAT = "%s绑定PV失败;";
    private static final String ERROR_CLUSTER_NOT_EXIST = "error.cluster.not.exist";
    private static final String GRAFANA_CLIENT_PREFIX = "grafana";

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
    private DevopsClusterService devopsClusterService;
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    private DevopsCertManagerMapper devopsCertManagerMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsPvcService devopsPvcService;
    @Autowired
    private DevopsPvService devopsPvService;
    @Autowired
    private DevopsEnvFileErrorService devopsEnvFileErrorService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsPvMapper devopsPvMapper;

    @Override
    public void baseCreate(DevopsClusterResourceDTO devopsClusterResourceDTO) {
        if (devopsClusterResourceMapper.insertSelective(devopsClusterResourceDTO) != 1) {
            throw new CommonException("error.insert.cluster.resource");
        }
    }

    @Override
    public void baseUpdate(DevopsClusterResourceDTO devopsClusterResourceDTO) {
        if (devopsClusterResourceMapper.updateByPrimaryKeySelective(devopsClusterResourceDTO) != 1) {
            throw new CommonException("error.update.cluster.resource");
        }
    }

    @Override
    public void createCertManager(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO1 = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        if (!ObjectUtils.isEmpty(devopsClusterResourceDTO1)) {
            throw new CommonException("error.create.cert.manager.exist");
        }
        DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
        devopsClusterResourceDTO.setType(ClusterResourceType.CERTMANAGER.getType());
        devopsClusterResourceDTO.setClusterId(clusterId);

        DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = new DevopsCertManagerRecordDTO();
        devopsCertManagerRecordDTO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
        MapperUtil.resultJudgedInsertSelective(devopsCertManagerRecordMapper, devopsCertManagerRecordDTO, "error.insert.cert.manager.record");
        //记录chart信息
        DevopsCertManagerDTO devopsCertManagerDTO = new DevopsCertManagerDTO();
        devopsCertManagerDTO.setNamespace(CertManagerConstants.CERT_MANAGER_REALASE_NAME_C7N);
        devopsCertManagerDTO.setChartVersion(CertManagerConstants.CERT_MANAGER_CHART_VERSION);
        MapperUtil.resultJudgedInsertSelective(devopsCertManagerMapper, devopsCertManagerDTO, "error.insert.cert.manager");
        // 插入数据
        devopsClusterResourceDTO.setObjectId(devopsCertManagerRecordDTO.getId());
        devopsClusterResourceDTO.setClusterId(clusterId);
        devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.INSTALL.getType());
        devopsClusterResourceDTO.setConfigId(devopsCertManagerDTO.getId());
        baseCreate(devopsClusterResourceDTO);
        // 让agent创建cert-mannager
        agentCommandService.createCertManager(clusterId);
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
    public Boolean deleteCertManager(Long clusterId) {
        if (checkCertManager(clusterId)) {
            return false;
        }

        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = devopsCertManagerRecordMapper.selectByPrimaryKey(devopsClusterResourceDTO.getObjectId());
        devopsCertManagerRecordDTO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
        devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.UNINSTALL.getType());
        devopsCertManagerRecordMapper.updateByPrimaryKey(devopsCertManagerRecordDTO);
        baseUpdate(devopsClusterResourceDTO);
        agentCommandService.unloadCertManager(clusterId);
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
        return CollectionUtils.isEmpty(ids) ? false : true;
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
        DevopsClusterResourceDTO prometheus = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        ClusterResourceVO clusterResourceVO = new ClusterResourceVO();
        if (ObjectUtils.isEmpty(prometheus)) {
            clusterResourceVO.setStatus(ClusterResourceStatus.UNINSTALLED.getStatus());
        } else {
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
            certificationDTOS.forEach(v -> devopsCertificationMapper.deleteByPrimaryKey(v.getId()));
        }
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        devopsCertManagerRecordMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getObjectId());
        devopsCertManagerMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getConfigId());
        devopsClusterResourceMapper.deleteByPrimaryKey(devopsClusterResourceDTO.getId());
    }

    @Override
    @Transactional
    public void basedeletePrometheus(Long clusterId) {
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
    public Boolean createPrometheus(Long projectId, Long clusterId, DevopsPrometheusVO devopsPrometheusVO) {
        DevopsClusterDTO devopsClusterDTO = checkClusterExist(clusterId);
        Long systemEnvId = devopsClusterDTO.getSystemEnvId();
        if (systemEnvId == null) {
            throw new CommonException("no.cluster.system.env");
        }
        // 校验环境相关信息

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(systemEnvId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);
        // 解决分布式事务问题，注册client
        ClientDTO clientDTO = baseServiceClientOperator.queryClientBySourceId(devopsClusterDTO.getOrganizationId(), devopsClusterDTO.getId());
        LOGGER.info("clientDTO:{}", clientDTO);
        // 集群未注册client，则先注册
        if (clientDTO == null) {
            clientDTO = registerClient(devopsClusterDTO);
        }
        devopsClusterDTO.setClientId(clientDTO.getId());
        devopsPrometheusVO.setClientName(clientDTO.getName());
        devopsClusterService.baseUpdate(devopsClusterDTO);

        DevopsPrometheusDTO newPrometheusDTO = ConvertUtils.convertObject(devopsPrometheusVO, DevopsPrometheusDTO.class);
        DevopsClusterResourceDTO clusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        //安装失败点击安装
        if (clusterResourceDTO != null) {
            retryPrometheusInstance(clusterResourceDTO.getObjectId(), systemEnvId);
        } else {
            //首次点击安装
            newPrometheusDTO.setClusterId(clusterId);
            if (devopsPrometheusMapper.insertSelective(newPrometheusDTO) != 1) {
                throw new CommonException("error.insert.prometheus");
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
        if (devopsClusterDTO.getSystemEnvId() == null) {
            throw new CommonException("error.cluster.system.envId.null");
        }
        DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(devopsPrometheusVO.getId());
        if (devopsPrometheusDTO.getAdminPassword().equals(devopsPrometheusVO.getAdminPassword())
                && devopsPrometheusDTO.getGrafanaDomain().equals(devopsPrometheusVO.getGrafanaDomain())) {
            return false;
        }

        devopsPrometheusDTO.setAdminPassword(devopsPrometheusVO.getAdminPassword());
        devopsPrometheusDTO.setGrafanaDomain(devopsPrometheusVO.getGrafanaDomain());
        if (devopsPrometheusMapper.updateByPrimaryKey(devopsPrometheusDTO) != 1) {
            throw new CommonException("error.prometheus.update");
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
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.queryPrometheusByClusterId(clusterId);
        DevopsPvDTO alertManagerPv = devopsPvMapper.selectByPrimaryKey(devopsPrometheusDTO.getAlertmanagerPvId());
        DevopsPvDTO grafanaPv = devopsPvMapper.selectByPrimaryKey(devopsPrometheusDTO.getGrafanaPvId());
        DevopsPvDTO prometheusPv = devopsPvMapper.selectByPrimaryKey(devopsPrometheusDTO.getPrometheusPvId());

        DevopsPrometheusVO devopsPrometheusVO = devopsPrometheusMapper.queryPrometheusWithPvById(devopsClusterResourceDTO.getConfigId());
        setPvStatus(alertManagerPv, "alertmanager", devopsPrometheusVO);
        setPvStatus(grafanaPv, "grafana", devopsPrometheusVO);
        setPvStatus(prometheusPv, "prometheus", devopsPrometheusVO);

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
    public Boolean uninstallPrometheus(Long clusterId) {
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
        DevopsClusterResourceDTO clusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        ClusterResourceVO clusterResourceVO = queryPrometheusStatus(projectId, clusterId);
        if (!clusterResourceVO.getStatus().equals(ClusterResourceStatus.AVAILABLE.getStatus())) {
            return null;
        }
        DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(clusterResourceDTO.getConfigId());
        String grafanaType = type.equals("node") ? GRAFANA_NODE : GRAFANA_CLUSTER;
        return String.format("%s%s%s", "http://", devopsPrometheusDTO.getGrafanaDomain(), grafanaType);
    }

    @Override
    public void installPrometheus(Long clusterId, DevopsPrometheusDTO devopsPrometheusDTO) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        DevopsClusterResourceDTO clusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
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
    public void retryInstallPrometheus(Long clusterId) {
        DevopsClusterResourceDTO clusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        if (clusterResourceDTO == null || clusterResourceDTO.getObjectId() == null) {
            throw new CommonException("error.prometheus.retry");
        } else {
            AppServiceInstanceDTO instanceDTO = appServiceInstanceService.baseQuery(clusterResourceDTO.getObjectId());
            retryPrometheusInstance(clusterResourceDTO.getObjectId(), instanceDTO.getEnvId());
        }
    }

    private void checkPodIsReady(Long instanceId, ClusterResourceVO clusterResourceVO) {
        List<DevopsEnvPodVO> devopsEnvPodDTOS = ConvertUtils.convertList(devopsEnvPodService.baseListByInstanceId(instanceId), DevopsEnvPodVO.class);
        //查询pod状态
        devopsEnvPodDTOS.forEach(devopsEnvPodVO -> devopsEnvPodService.fillContainers(devopsEnvPodVO));
        List<ContainerVO> readyPod = new ArrayList<>();
        //健康检查，ready=true的pod大于1就是可用的
        devopsEnvPodDTOS.forEach(devopsEnvPodVO -> {
            if (Boolean.TRUE.equals(devopsEnvPodVO.getReady())) {
                readyPod.addAll(devopsEnvPodVO.getContainers().stream().filter(ContainerVO::getReady).collect(Collectors.toList()));
            }
        });
        if (!readyPod.isEmpty()) {
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

    private DevopsPvcReqVO operatePV(Long pvId, Long envId, String name) {
        DevopsPvcReqVO devopsPvcReqVO = new DevopsPvcReqVO();
        DevopsPvVO devopsPvVO = devopsPvService.queryById(pvId);
        devopsPvcReqVO.setPvId(devopsPvVO.getId());
        devopsPvcReqVO.setName(name + "-" + GenerateUUID.generateUUID().substring(0, 10));
        devopsPvcReqVO.setAccessModes(devopsPvVO.getAccessModes());
        devopsPvcReqVO.setRequestResource(devopsPvVO.getRequestResource());
        devopsPvcReqVO.setEnvId(envId);
        return devopsPvcReqVO;
    }

    private DevopsClusterDTO checkClusterExist(Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO == null) {
            throw new CommonException(ERROR_CLUSTER_NOT_EXIST);
        }
        return devopsClusterDTO;
    }

    private ClientDTO registerClient(DevopsClusterDTO devopsClusterDTO) {
        // 添加客户端
        ClientVO clientVO = new ClientVO();
        clientVO.setName(GRAFANA_CLIENT_PREFIX + devopsClusterDTO.getId());
        clientVO.setOrganizationId(devopsClusterDTO.getOrganizationId());
        clientVO.setAuthorizedGrantTypes("password,implicit,client_credentials,refresh_token,authorization_code");
        clientVO.setSecret("grafana");
        clientVO.setRefreshTokenValidity(360000L);
        clientVO.setAccessTokenValidity(360000L);
        clientVO.setSourceId(devopsClusterDTO.getId());
        clientVO.setSourceType("cluster");
        LOGGER.info("clientVO:{}", clientVO);
        return baseServiceClientOperator.createClient(devopsClusterDTO.getOrganizationId(), clientVO);
    }

    private void updatePVForPromethues(DevopsPrometheusDTO newPrometheusDTO,
                                       DevopsPrometheusDTO oldPrometheusDTO,
                                       List<DevopsPvcDTO> pvcDTOList,
                                       Long systemEnvId,
                                       Long projectId) {
        if (!newPrometheusDTO.getPrometheusPvId().equals(oldPrometheusDTO.getPrometheusPvId())) {
            devopsPvcService.delete(pvcDTOList.get(0).getEnvId(), pvcDTOList.get(0).getId());
            DevopsPvcReqVO prometheusPvcReqVO = operatePV(newPrometheusDTO.getPrometheusPvId(), systemEnvId, PrometheusPVCTypeEnum.PROMETHEUS_PVC.value());
            devopsPvcService.create(projectId, prometheusPvcReqVO);
            pvcDTOList.remove(0);
        }

        if (!newPrometheusDTO.getAlertmanagerPvId().equals(oldPrometheusDTO.getAlertmanagerPvId())) {
            devopsPvcService.delete(pvcDTOList.get(0).getEnvId(), pvcDTOList.get(0).getId());
            DevopsPvcReqVO prometheusPvcReqVO = operatePV(newPrometheusDTO.getAlertmanagerPvId(), systemEnvId, PrometheusPVCTypeEnum.ALERTMANAGER_PVC.value());
            devopsPvcService.create(projectId, prometheusPvcReqVO);
            pvcDTOList.remove(0);
        }

        if (!newPrometheusDTO.getGrafanaPvId().equals(oldPrometheusDTO.getGrafanaPvId())) {
            devopsPvcService.delete(pvcDTOList.get(0).getEnvId(), pvcDTOList.get(0).getId());
            DevopsPvcReqVO prometheusPvcReqVO = operatePV(newPrometheusDTO.getGrafanaPvId(), systemEnvId, PrometheusPVCTypeEnum.ALERTMANAGER_PVC.value());
            devopsPvcService.create(projectId, prometheusPvcReqVO);
            pvcDTOList.remove(0);
        }

    }

    private void setPvStatus(DevopsPvDTO devopsPvDTO, String type, DevopsPrometheusVO devopsPrometheusVO) {
        String boundPVCName = devopsPvDTO.getPvcName();
        String pvName = devopsPvDTO.getName();
        if (boundPVCName != null && boundPVCName.contains(type)) {
            switch (type) {
                case "alertmanager":
                    devopsPrometheusVO.setAlertmanagerPvStatus(devopsPvDTO.getStatus());
                    devopsPrometheusVO.setAlertmanagerPvId(devopsPvDTO.getId());
                    devopsPrometheusVO.setAlertmanagerPvName(pvName);
                    break;
                case "grafana":
                    devopsPrometheusVO.setGrafanaPvStatus(devopsPvDTO.getStatus());
                    devopsPrometheusVO.setGrafanaPvId(devopsPvDTO.getId());
                    devopsPrometheusVO.setGrafanaPvName(pvName);
                    break;
                case "prometheus":
                    devopsPrometheusVO.setPrometheusPvStatus(devopsPvDTO.getStatus());
                    devopsPrometheusVO.setPrometheusPvId(devopsPvDTO.getId());
                    devopsPrometheusVO.setPrometheusPvName(pvName);
                    break;
                default:
            }
        }
    }
}
