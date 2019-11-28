package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.constants.CertManagerConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.PrometheusConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import io.choerodon.devops.infra.dto.iam.ClientDTO;
import io.choerodon.devops.infra.dto.iam.ClientVO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
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
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsGitService devopsGitService;
    @Autowired
    private DevopsEnvCommitService devopsEnvCommitService;

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
        DevopsClusterResourceDTO clusterResourceDTO = devopsClusterResourceMapper.selectOne(devopsClusterResourceDTO);

        DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = new DevopsCertManagerRecordDTO();
        devopsCertManagerRecordDTO.setStatus(ClusterResourceStatus.PROCESSING.getStatus());
        devopsCertManagerRecordMapper.insertSelective(devopsCertManagerRecordDTO);
        //记录chart信息
        DevopsCertManagerDTO devopsCertManagerDTO = new DevopsCertManagerDTO();
        devopsCertManagerDTO.setNamespace(CertManagerConstants.CERT_MANAGER_REALASE_NAME_C7N);
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
        if (checkCertManager(clusterId).getCheckCert()) {
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
    public CertManagerMsgVO checkCertManager(Long clusterId) {
        CertManagerMsgVO certManagerMsgVO = new CertManagerMsgVO(true);
        List<CertificationDTO> certificationDTOS = devopsCertificationMapper.listClusterCertification(clusterId);
        if (CollectionUtils.isEmpty(certificationDTOS)) {
            certManagerMsgVO.setCheckCert(false);
            return certManagerMsgVO;
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
        if (CollectionUtils.isEmpty(ids)) {
            certManagerMsgVO.setCheckCert(false);
        }
        return certManagerMsgVO;
    }


    @Override
    public DevopsClusterResourceDTO queryByClusterIdAndType(Long clusterId, String type) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(clusterId, type);
        return devopsClusterResourceDTO;
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
        // 解决分布式事务问题，注册client成功，更新cluster表client字段失败
        if (devopsClusterDTO.getClientId() == null) {
            ClientDTO clientDTO = baseServiceClientOperator.queryClientBySourceId(devopsClusterDTO.getOrganizationId(), devopsClusterDTO.getId());
            // 集群未注册client，则先注册
            if (clientDTO == null) {
                clientDTO = registerClient(devopsClusterDTO);
            }
            devopsClusterDTO.setClientId(clientDTO.getId());
            devopsClusterService.baseUpdate(devopsClusterDTO);
        }
        DevopsPrometheusDTO devopsPrometheusDTO = ConvertUtils.convertObject(devopsPrometheusVO, DevopsPrometheusDTO.class);
        DevopsClusterResourceDTO devopsClusterResource = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        if (devopsClusterResource != null) {
            throw new CommonException("error.prometheus.already.exist");
        }

        devopsPrometheusDTO.setClusterId(clusterId);
        if (devopsPrometheusMapper.insertSelective(devopsPrometheusDTO) != 1) {
            throw new CommonException("error.insert.prometheus");
        }

        DevopsClusterResourceDTO devopsClusterResourceDTO = new DevopsClusterResourceDTO();
        devopsClusterResourceDTO.setClusterId(clusterId);
        devopsClusterResourceDTO.setConfigId(devopsPrometheusDTO.getId());
        devopsClusterResourceDTO.setName(devopsClusterDTO.getName());
        devopsClusterResourceDTO.setCode(devopsClusterDTO.getCode());
        devopsClusterResourceDTO.setType(ClusterResourceType.PROMETHEUS.getType());
        devopsClusterResourceDTO.setOperate(ClusterResourceOperateType.INSTALL.getType());
        devopsClusterResourceService.baseCreate(devopsClusterResourceDTO);

        // 创建PVC
        createPVC(projectId, devopsPrometheusVO, systemEnvId, clusterId);
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
        addPvcList(devopsPrometheusDTO);
        DevopsClusterResourceDTO clusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        clusterResourceDTO.setOperate(ClusterResourceOperateType.UPGRADE.getType());

        // 升级prometheus版本
        AppServiceInstanceDTO appServiceInstanceDTO = componentReleaseService.updateReleaseForPrometheus(devopsPrometheusDTO, clusterResourceDTO.getObjectId(), devopsClusterDTO.getSystemEnvId());
        clusterResourceDTO.setObjectId(appServiceInstanceDTO.getId());
        devopsClusterResourceService.baseUpdate(clusterResourceDTO);
        return true;
    }


    @Override
    public DevopsPrometheusVO queryPrometheus(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());

        return devopsPrometheusMapper.queryPrometheusWithPvById(devopsClusterResourceDTO.getConfigId());
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
                PrometheusDeploy.WAITING.getStaus(),
                PrometheusDeploy.WAITING.getStaus(),
                PrometheusDeploy.WAITING.getStaus());
        StringBuilder errorStr = new StringBuilder();
        //1.校验pvc解析与绑定
        DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(devopsClusterResourceDTO.getConfigId());
        getPvcsStatus(devopsPrometheusDTO, prometheusStageVO);
        if (!PrometheusDeploy.SUCCESSED.getStaus().equals(prometheusStageVO.getBoundPvc())) {
            return prometheusStageVO;
        }

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
    @Transactional
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
                //验证pvc是否存在,三个pvc都不存在才去删除prometheus
                DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(devopsClusterResourceDTO.getConfigId());
                if (devopsPrometheusDTO == null) {
                    clusterResourceVO.setStatus(ClusterResourceStatus.UNINSTALLED.getStatus());
                    return clusterResourceVO;
                }
                DevopsPvcDTO pormetheusPVC = devopsPvcService.queryById(devopsPrometheusDTO.getPrometheusPvId());
                DevopsPvcDTO grafanaPVC = devopsPvcService.queryById(devopsPrometheusDTO.getGrafanaPvId());
                DevopsPvcDTO alertmanagerPVC = devopsPvcService.queryById(devopsPrometheusDTO.getAlertmanagerPvId());
                if (pormetheusPVC == null && grafanaPVC == null && alertmanagerPVC == null) {
                    basedeletePrometheus(clusterId);
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
        if (prometheusStageVO.getParserPvc().equals(PrometheusDeploy.FAILED.getStaus())
                || prometheusStageVO.getBoundPvc().equals(PrometheusDeploy.FAILED.getStaus())
                || prometheusStageVO.getParserPrometheus().equals(PrometheusDeploy.FAILED.getStaus())
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
        if (clusterResourceDTO == null) {
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
        AppServiceInstanceDTO appServiceInstanceDTO = null;
        devopsPrometheusDTO.setClusterCode(devopsClusterDTO.getCode());
        appServiceInstanceDTO = componentReleaseService.createReleaseForPrometheus(devopsClusterDTO.getSystemEnvId(), devopsPrometheusDTO);
        clusterResourceDTO.setObjectId(appServiceInstanceDTO.getId());
        devopsClusterResourceService.baseUpdate(clusterResourceDTO);
    }

    @Override
    public Boolean queryCertManagerByEnvId(Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceMapper.queryByClusterIdAndType(devopsEnvironmentDTO.getClusterId(), ClusterResourceType.CERTMANAGER.getType());
        return !ObjectUtils.isEmpty(devopsClusterResourceDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePvc(Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = queryByClusterIdAndType(clusterId, ClusterResourceType.PROMETHEUS.getType());
        if (devopsClusterResourceDTO != null) {
            DevopsPrometheusDTO devopsPrometheusDTO = devopsPrometheusMapper.selectByPrimaryKey(devopsClusterResourceDTO.getConfigId());
            DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
            DevopsPvcDTO pormetheusPvcDTO = devopsPvcService.queryByPvId(devopsPrometheusDTO.getPrometheusPvId());
            DevopsPvcDTO grafanaPvcDTO = devopsPvcService.queryByPvId(devopsPrometheusDTO.getGrafanaPvId());
            DevopsPvcDTO alertmanagerDTO = devopsPvcService.queryByPvId(devopsPrometheusDTO.getAlertmanagerPvId());
            devopsPvcService.delete(devopsClusterDTO.getSystemEnvId(), pormetheusPvcDTO.getId());
            devopsPvcService.delete(devopsClusterDTO.getSystemEnvId(), grafanaPvcDTO.getId());
            devopsPvcService.delete(devopsClusterDTO.getSystemEnvId(), alertmanagerDTO.getId());
        }
    }

    @Override
    public void retryPvc(List<Long> pvcIds) {
        pvcIds.forEach(pvcId -> devopsPvcService.retryPushPvcToGitLab(pvcId));
    }

    @Override
    public void retryPrometheusInstance(Long instanceId, Long envId) {
        // 三步重试每次只重试一步
        if (componentReleaseService.retryPushingToGitLab(instanceId, ClusterResourceType.PROMETHEUS)) {
            return;
        }

        if (retrySystemEnvGitOps(envId)) {
            return;
        }

        if (componentReleaseService.restartReleaseInstance(instanceId)) {
            return;
        }
    }

    // 开启新事务
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean retrySystemEnvGitOps(Long envId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        if (devopsEnvironmentDTO == null) {
            LOGGER.info("Retry cluster env GitOps: the environment with id {} is unexpectedly null", envId);
            return false;
        }

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId().longValue());
        if (userAttrDTO == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }

        // 查询GitLab上环境最新的commit
        CommitDTO commitDO = gitlabServiceClientOperator.listCommits(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue(), userAttrDTO.getGitlabUserId().intValue(), 1, 1).get(0);

        // 当环境总览第一阶段为空，第一阶段的commit不是最新commit, 第一阶段和第二阶段commit不一致时，可以重新触发gitOps
        if (GitOpsUtil.isToRetryGitOps(
                devopsEnvironmentDTO.getSagaSyncCommit(),
                devopsEnvCommitService.baseQuery(devopsEnvironmentDTO.getSagaSyncCommit()).getCommitSha(),
                devopsEnvironmentDTO.getDevopsSyncCommit(), commitDO.getId())) {

            PushWebHookVO pushWebHookVO = new PushWebHookVO();
            pushWebHookVO.setCheckoutSha(commitDO.getId());
            pushWebHookVO.setUserId(userAttrDTO.getGitlabUserId().intValue());
            pushWebHookVO.setProjectId(devopsEnvironmentDTO.getGitlabEnvProjectId().intValue());
            CommitVO commitDTO = new CommitVO();
            commitDTO.setId(commitDO.getId());
            commitDTO.setTimestamp(commitDO.getTimestamp());
            pushWebHookVO.setCommits(ArrayUtil.singleAsList(commitDTO));

            devopsGitService.fileResourceSyncSaga(pushWebHookVO, devopsEnvironmentDTO.getToken());
            return true;
        }
        return false;
    }

    private void checkPodIsReady(Long instanceId, ClusterResourceVO clusterResourceVO) {
        List<DevopsEnvPodVO> devopsEnvPodDTOS = ConvertUtils.convertList(devopsEnvPodService.baseListByInstanceId(instanceId), DevopsEnvPodVO.class);
        //查询pod状态
        devopsEnvPodDTOS.stream().forEach(devopsEnvPodVO -> devopsEnvPodService.fillContainers(devopsEnvPodVO));
        List<ContainerVO> readyPod = new ArrayList<>();
        //健康检查，ready=true的pod大于1就是可用的
        devopsEnvPodDTOS.stream().forEach(devopsEnvPodVO -> {
            if (Boolean.TRUE.equals(devopsEnvPodVO.getReady())) {
                readyPod.addAll(devopsEnvPodVO.getContainers().stream().filter(pod -> pod.getReady() == true).collect(Collectors.toList()));
            }
        });
        if (readyPod.size() >= 1) {
            clusterResourceVO.setStatus(ClusterResourceStatus.AVAILABLE.getStatus());
        } else {
            clusterResourceVO.setStatus(ClusterResourceStatus.DISABLED.getStatus());
        }
    }

    private PrometheusStageVO getPvcsStatus(DevopsPrometheusDTO devopsPrometheusDTO, PrometheusStageVO prometheusStageVO) {
        DevopsPvcDTO pormetheusPVC = devopsPvcService.queryById(devopsPrometheusDTO.getPrometheusPvId());
        DevopsPvcDTO grafanaPVC = devopsPvcService.queryById(devopsPrometheusDTO.getGrafanaPvId());
        DevopsPvcDTO alertmanagerPVC = devopsPvcService.queryById(devopsPrometheusDTO.getAlertmanagerPvId());
        StringBuilder errorStr = new StringBuilder();

        // 1）是否成功创建了相关pvc记录
        if (pormetheusPVC == null || grafanaPVC == null || alertmanagerPVC == null) {
            return prometheusStageVO;
        }
        // 2）判断pvc是否绑定成功
        if (PvcStatus.BOUND.getStatus().equals(pormetheusPVC.getStatus())
                && PvcStatus.BOUND.getStatus().equals(grafanaPVC.getStatus())
                && PvcStatus.BOUND.getStatus().equals(alertmanagerPVC.getStatus())) {
            prometheusStageVO.setParserPvc(PrometheusDeploy.SUCCESSED.getStaus());
            prometheusStageVO.setBoundPvc(PrometheusDeploy.SUCCESSED.getStaus());
            return prometheusStageVO;
        }
        // 3）判断pvc是否解析完成
        List<DevopsPvcDTO> pvcDTOList = new ArrayList<>();
        pvcDTOList.add(pormetheusPVC);
        pvcDTOList.add(grafanaPVC);
        pvcDTOList.add(alertmanagerPVC);
        if (PvcStatus.OPERATING.getStatus().equals(pormetheusPVC.getStatus())
                || PvcStatus.OPERATING.getStatus().equals(grafanaPVC.getStatus())
                || PvcStatus.OPERATING.getStatus().equals(alertmanagerPVC.getStatus())) {
            int parserSuccessPvc = 0;
            for (DevopsPvcDTO pvcDTO : pvcDTOList) {
                String parserStatus = getParserStatus(pvcDTO.getCommandId(), pvcDTO.getEnvId());
                if (parserStatus.equals(PrometheusDeploy.FAILED.getStaus())) {
                    // pvc解析失败
                    prometheusStageVO.setParserPvc(PrometheusDeploy.FAILED.getStaus());
                    errorStr.append(getErrorDetail(pvcDTO.getCommandId(), pvcDTO.getEnvId()));
                    prometheusStageVO.setError(errorStr.toString());
                    return prometheusStageVO;
                } else if (parserStatus.equals(PrometheusDeploy.SUCCESSED.getStaus())) {
                    parserSuccessPvc = parserSuccessPvc + 1;
                }
            }
            //pvc是否全部解析成功，否则状态为处理中
            if (parserSuccessPvc == 3) {
                prometheusStageVO.setParserPvc(PrometheusDeploy.SUCCESSED.getStaus());
                prometheusStageVO.setBoundPvc(PrometheusDeploy.OPERATING.getStaus());
            } else {
                prometheusStageVO.setParserPvc(PrometheusDeploy.OPERATING.getStaus());
            }

        } else if (PvcStatus.FAILED.getStatus().equals(pormetheusPVC.getStatus())
                || PvcStatus.FAILED.getStatus().equals(grafanaPVC.getStatus())
                || PvcStatus.FAILED.getStatus().equals(alertmanagerPVC.getStatus())) {
            // 4)判断k8s是否创建pvc失败
            prometheusStageVO.setParserPvc(PrometheusDeploy.FAILED.getStaus());
            pvcDTOList.stream().filter(pvcDTO -> pvcDTO.getStatus().equals(PvcStatus.FAILED.getStatus()))
                    .forEach(pvcDTO -> {
                        DevopsEnvCommandDTO prometheusCommand = devopsEnvCommandService.baseQuery(pvcDTO.getCommandId());
                        errorStr.append(String.format(ERROR_FORMAT, prometheusCommand.getError()));
                    });
        } else {
            // 5）判断pvc是否绑定失败
            prometheusStageVO.setParserPvc(PrometheusDeploy.SUCCESSED.getStaus());
            prometheusStageVO.setBoundPvc(PrometheusDeploy.OPERATING.getStaus());
            if (PvcStatus.PENDING.getStatus().equals(pormetheusPVC.getStatus())
                    || PvcStatus.PENDING.getStatus().equals(grafanaPVC.getStatus())
                    || PvcStatus.PENDING.getStatus().equals(alertmanagerPVC.getStatus())) {
                prometheusStageVO.setBoundPvc(PrometheusDeploy.FAILED.getStaus());
                for (DevopsPvcDTO pvcDTO : pvcDTOList) {
                    if (pvcDTO.getStatus().equals(PvcStatus.PENDING.getStatus())) {
                        DevopsPvDTO pvDTO = devopsPvService.baseQueryById(pvcDTO.getPvId());
                        if (!pvDTO.getPvcName().equals(pvcDTO.getName())) {
                            errorStr.append(String.format(ERROR_BOUND_PVC_FORMAT, pvcDTO.getName()));
                            prometheusStageVO.setBoundPvc(PrometheusDeploy.FAILED.getStaus());
                            break;
                        }
                    }
                }
            } else {
                prometheusStageVO.setBoundPvc(PrometheusDeploy.OPERATING.getStaus());
            }
        }
        prometheusStageVO.setError(errorStr.toString());
        return prometheusStageVO;
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
     * 获取pvc和promethuex 解析结果
     *
     * @param commandId
     * @param envId
     * @return
     */
    private String getParserStatus(Long commandId, Long envId) {
        DevopsEnvCommandDTO prometheusCommand = devopsEnvCommandService.baseQuery(commandId);
        if (prometheusCommand.getSha() != null) {
            return PrometheusDeploy.SUCCESSED.getStaus();
        } else {
            List<DevopsEnvFileErrorDTO> fileErrorDTOList = devopsEnvFileErrorService.baseListByEnvId(envId);
            if (CommandStatus.OPERATING.getStatus().equals(prometheusCommand.getStatus())
                    && (fileErrorDTOList == null || fileErrorDTOList.size() == 0)) {
                return PrometheusDeploy.OPERATING.getStaus();
            } else {
                return PrometheusDeploy.FAILED.getStaus();
            }
        }
    }

    private void addPvcList(DevopsPrometheusDTO devopsPrometheusDTO) {
        DevopsPvcDTO prometheusPvcDTO = devopsPvcService.queryByPvId(devopsPrometheusDTO.getPrometheusPvId());
        DevopsPvcDTO grafanaPvcDTO = devopsPvcService.queryByPvId(devopsPrometheusDTO.getGrafanaPvId());
        DevopsPvcDTO alertmanagerDTO = devopsPvcService.queryByPvId(devopsPrometheusDTO.getAlertmanagerPvId());
        List<DevopsPvcDTO> devopsPvcDTOList = new ArrayList<>();
        devopsPvcDTOList.add(prometheusPvcDTO);
        devopsPvcDTOList.add(grafanaPvcDTO);
        devopsPvcDTOList.add(alertmanagerDTO);
        devopsPrometheusDTO.setDevopsPvcList(devopsPvcDTOList);
    }

    private Boolean checkValidity(Date date, Date validFrom, Date validUntil) {
        return validFrom != null && validUntil != null
                && date.after(validFrom) && date.before(validUntil);
    }

    private DevopsPvcReqVO operatePV(Long pvId, Long envId, String name, Long clusterId) {
        DevopsPvcReqVO devopsPvcReqVO = new DevopsPvcReqVO();
        DevopsPvVO devopsPvVO = devopsPvService.queryById(pvId);
        devopsPvcReqVO.setPvId(devopsPvVO.getId());
        devopsPvcReqVO.setName(name + "-" + clusterId);
        devopsPvcReqVO.setAccessModes(devopsPvVO.getAccessModes());
        devopsPvcReqVO.setRequestResource(devopsPvVO.getRequestResource());
        devopsPvcReqVO.setEnvId(envId);
        return devopsPvcReqVO;
    }

    /**
     * 创建与安装Prometheus相关的PVC
     *
     * @param projectId
     * @param devopsPrometheusVO
     * @param clusterId
     * @param systemEnvId
     */
    private void createPVC(Long projectId, DevopsPrometheusVO devopsPrometheusVO, Long systemEnvId, Long clusterId) {
        DevopsPvcReqVO prometheusPvcReqVO = operatePV(devopsPrometheusVO.getPrometheusPvId(), systemEnvId, PrometheusPVCTypeEnum.PROMETHEUS_PVC.value(), clusterId);
        DevopsPvcReqVO grafanaPvcReqVO = operatePV(devopsPrometheusVO.getGrafanaPvId(), systemEnvId, PrometheusPVCTypeEnum.GRAFANA_PVC.value(), clusterId);
        DevopsPvcReqVO alertmanagerPvcReqVO = operatePV(devopsPrometheusVO.getAlertmanagerPvId(), systemEnvId, PrometheusPVCTypeEnum.ALERTMANAGER_PVC.value(), clusterId);
        devopsPvcService.create(projectId, prometheusPvcReqVO);
        devopsPvcService.create(projectId, grafanaPvcReqVO);
        devopsPvcService.create(projectId, alertmanagerPvcReqVO);
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
        clientVO.setName(devopsClusterDTO.getChoerodonId());
        clientVO.setOrganizationId(devopsClusterDTO.getOrganizationId());
        clientVO.setAuthorizedGrantTypes("password,implicit,client_credentials,refresh_token,authorization_code");
        clientVO.setSecret(GenerateUUID.generateUUID().substring(0, 16).replace("-", "A"));
        clientVO.setRefreshTokenValidity(360000L);
        clientVO.setAccessTokenValidity(360000L);
        clientVO.setSourceId(devopsClusterDTO.getId());
        clientVO.setSourceType("cluster");
        return baseServiceClientOperator.createClient(devopsClusterDTO.getOrganizationId(), clientVO);
    }
}
