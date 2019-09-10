package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.TestReleaseStatusPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.*;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Zenger on 2018/4/17.
 */
@Service
public class AgentMsgHandlerServiceImpl implements AgentMsgHandlerService {

    public static final String CREATE_TYPE = "create";
    public static final String UPDATE_TYPE = "update";
    private static final String CHOERODON_IO_NETWORK_SERVICE_INSTANCES = "choerodon.io/network-service-instances";
    private static final String SERVICE_LABLE = "choerodon.io/network";
    private static final String PENDING = "Pending";
    private static final String METADATA = "metadata";
    private static final String SERVICE_KIND = "service";
    private static final String INGRESS_KIND = "ingress";
    private static final String INSTANCE_KIND = "instance";
    private static final String CONFIGMAP_KIND = "configmap";
    private static final String C7NHELMRELEASE_KIND = "c7nhelmrelease";
    private static final String CERTIFICATE_KIND = "certificate";
    private static final String SECRET_KIND = "secret";
    private static final String PUBLIC = "public";
    private static final Logger logger = LoggerFactory.getLogger(AgentMsgHandlerServiceImpl.class);
    private static final String RESOURCE_VERSION = "resourceVersion";
    private static final String ENV_NOT_EXIST = "env not exists: {}";
    public static final String EVICTED = "Evicted";
    private static JSON json = new JSON();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private ObjectMapper mapper = new ObjectMapper();
    private Gson gson = new Gson();

    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${agent.repoUrl}")
    private String agentRepoUrl;


    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    @Autowired
    private DevopsServiceInstanceService devopsServiceInstanceService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsEnvCommandLogService devopsEnvCommandLogService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsEnvFileService devopsEnvFileService;
    @Autowired
    private DevopsEnvCommitService devopsEnvCommitService;
    @Autowired
    private DevopsEnvFileErrorService devopsEnvFileErrorService;
    @Autowired
    private CertificationService certificationService;
    @Autowired
    private DevopsSecretService devopsSecretService;
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private DevopsClusterProPermissionService devopsClusterProPermissionService;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private DevopsConfigMapService devopsConfigMapService;
    @Autowired
    private ClusterNodeInfoService clusterNodeInfoService;
    @Autowired
    private DevopsRegistrySecretService devopsRegistrySecretService;
    @Autowired
    private DevopsCustomizeResourceService devopsCustomizeResourceService;
    @Autowired
    private AgentPodService agentPodService;
    @Autowired
    private AgentCommandService agentCommandService;
    @Autowired
    DevopsCommandEventService devopsCommandEventService;


    public void handlerUpdatePodMessage(String key, String msg, Long envId) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);

        AppServiceInstanceDTO appServiceInstanceDTO =
                appServiceInstanceService.baseQueryByCodeAndEnv(KeyParseUtil.getReleaseName(key), envId);
        if (appServiceInstanceDTO == null) {
            logger.info("instance not found");
            return;
        }
        DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
        DevopsEnvResourceDTO newDevopsEnvResourceDTO =
                devopsEnvResourceService.baseQueryOptions(
                        appServiceInstanceDTO.getId(),
                        null,
                        null,
                        KeyParseUtil.getResourceType(key),
                        v1Pod.getMetadata().getName());
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO();
        devopsEnvResourceDetailDTO.setMessage(msg);
        devopsEnvResourceDTO.setKind(KeyParseUtil.getResourceType(key));
        devopsEnvResourceDTO.setEnvId(envId);
        devopsEnvResourceDTO.setName(v1Pod.getMetadata().getName());
        devopsEnvResourceDTO.setReversion(TypeUtil.objToLong(v1Pod.getMetadata().getResourceVersion()));
        List<V1OwnerReference> v1OwnerReferences = v1Pod.getMetadata().getOwnerReferences();
        if (v1OwnerReferences == null || v1OwnerReferences.isEmpty()) {
            return;
        }
        if (v1OwnerReferences.get(0).getKind().equals(ResourceType.JOB.getType())) {
            return;
        }
        saveOrUpdateResource(devopsEnvResourceDTO,
                newDevopsEnvResourceDTO,
                devopsEnvResourceDetailDTO,
                appServiceInstanceDTO);
        String status = K8sUtil.changePodStatus(v1Pod);
        String resourceVersion = v1Pod.getMetadata().getResourceVersion();

        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setName(v1Pod.getMetadata().getName());
        devopsEnvPodDTO.setIp(v1Pod.getStatus().getPodIP());
        devopsEnvPodDTO.setStatus(status);
        devopsEnvPodDTO.setResourceVersion(resourceVersion);
        devopsEnvPodDTO.setNamespace(v1Pod.getMetadata().getNamespace());
        devopsEnvPodDTO.setReady(getReadyValue(status, v1Pod));
        devopsEnvPodDTO.setNodeName(v1Pod.getSpec().getNodeName());
        devopsEnvPodDTO.setRestartCount(K8sUtil.getRestartCountForPod(v1Pod));

        Boolean flag = false;
        if (appServiceInstanceDTO.getId() != null) {
            List<DevopsEnvPodDTO> devopsEnvPodEList = devopsEnvPodService
                    .baseListByInstanceId(appServiceInstanceDTO.getId());
            handleEnvPod(v1Pod, appServiceInstanceDTO, resourceVersion, devopsEnvPodDTO, flag, devopsEnvPodEList);
        }
    }


    /**
     * 当状态不等于Pending时，只有所有container都ready才是ready，即值为true
     * 方法中除了<code>v1Pod.getStatus().getContainerStatuses().isReady</code>的值，不对其他字段进行非空校验
     *
     * @param podStatus pod status
     * @param v1Pod     pod 对象，不能为空
     * @return true 当状态不等于Pending时，所有container都ready
     */
    private Boolean getReadyValue(String podStatus, V1Pod v1Pod) {
        return !PENDING.equals(podStatus) && v1Pod.getStatus().getContainerStatuses().stream().map(V1ContainerStatus::isReady).reduce((one, another) -> mapNullToFalse(one) && mapNullToFalse(another)).orElse(Boolean.FALSE);
    }


    /**
     * map null of type {@link Boolean} to primitive false
     *
     * @param value Boolean value
     * @return false if the value is null or false
     */
    private boolean mapNullToFalse(Boolean value) {
        return value != null && value;
    }


    private void handleEnvPod(V1Pod v1Pod, AppServiceInstanceDTO appServiceInstanceDTO, String resourceVersion, DevopsEnvPodDTO devopsEnvPodDTO, Boolean flag, List<DevopsEnvPodDTO> devopsEnvPodDTOS) {

        if (devopsEnvPodDTOS == null || devopsEnvPodDTOS.isEmpty()) {
            devopsEnvPodDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsEnvPodService.baseCreate(devopsEnvPodDTO);
        } else {
            for (DevopsEnvPodDTO pod : devopsEnvPodDTOS) {
                if (pod.getName().equals(v1Pod.getMetadata().getName())
                        && pod.getNamespace().equals(v1Pod.getMetadata().getNamespace())) {
                    if ((v1Pod.getStatus().getPhase() != null && v1Pod.getStatus().getPhase().equals(EVICTED)) || (v1Pod.getStatus().getReason() != null && v1Pod.getStatus().getReason().equals(EVICTED))) {
                        devopsEnvPodService.baseDeleteById(pod.getId());
                        devopsEnvResourceService.deleteByKindAndNameAndInstanceId(ResourceType.POD.getType(), pod.getName(), pod.getInstanceId());
                    } else if (!resourceVersion.equals(pod.getResourceVersion())) {
                        devopsEnvPodDTO.setId(pod.getId());
                        devopsEnvPodDTO.setInstanceId(pod.getInstanceId());
                        devopsEnvPodDTO.setObjectVersionNumber(pod.getObjectVersionNumber());
                        devopsEnvPodService.baseUpdate(devopsEnvPodDTO);
                    }
                    flag = true;
                }
            }
            if (!flag) {
                devopsEnvPodDTO.setInstanceId(appServiceInstanceDTO.getId());
                devopsEnvPodService.baseCreate(devopsEnvPodDTO);
            }
        }
    }

    @Override
    public void helmInstallResourceInfo(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        ReleasePayloadVO releasePayloadVO = JSONArray.parseObject(msg, ReleasePayloadVO.class);
        List<Resource> resources = JSONArray.parseArray(releasePayloadVO.getResources(), Resource.class);
        String releaseName = releasePayloadVO.getName();
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
        if (appServiceInstanceDTO != null) {
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                    .baseQuery(appServiceInstanceDTO.getCommandId());
            if (devopsEnvCommandDTO != null) {
                devopsEnvCommandDTO.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
                AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQueryByAppIdAndVersion(appServiceInstanceDTO.getAppServiceId(), releasePayloadVO.getChartVersion());
                appServiceInstanceDTO.setAppServiceVersionId(appServiceVersionDTO.getId());
                appServiceInstanceDTO.setStatus(InstanceStatus.RUNNING.getStatus());
                appServiceInstanceService.baseUpdate(appServiceInstanceDTO);
                installResource(resources, appServiceInstanceDTO);

            }
        }
    }


    @Override
    public void helmInstallJobInfo(String key, String msg, Long clusterId) {
        if (msg.equals("null")) {
            return;
        }
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        List<Job> jobs = JSONArray.parseArray(msg, Job.class);
        AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
        try {
            for (Job job : jobs) {
                appServiceInstanceDTO = appServiceInstanceService
                        .baseQueryByCodeAndEnv(job.getReleaseName(), envId);
                DevopsEnvResourceDTO newDevopsEnvResourceDTO =
                        devopsEnvResourceService.baseQueryOptions(
                                appServiceInstanceDTO.getId(),
                                appServiceInstanceDTO.getCommandId(),
                                envId,
                                job.getKind(),
                                job.getName());
                DevopsEnvResourceDTO devopsEnvResourceDTO =
                        new DevopsEnvResourceDTO();
                devopsEnvResourceDTO.setKind(job.getKind());
                devopsEnvResourceDTO.setName(job.getName());
                devopsEnvResourceDTO.setEnvId(envId);
                devopsEnvResourceDTO.setWeight(TypeUtil.objToLong(job.getWeight()));
                DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO();
                devopsEnvResourceDetailDTO.setMessage(
                        FileUtil.yamlStringtoJson(job.getManifest()));
                saveOrUpdateResource(devopsEnvResourceDTO, newDevopsEnvResourceDTO, devopsEnvResourceDetailDTO, appServiceInstanceDTO);
            }
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                    .baseQuery(appServiceInstanceDTO.getCommandId());
            devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        } catch (Exception e) {
            throw new CommonException("error.resource.insert", e);
        }
    }

    @Override
    public void resourceUpdate(String key, String msg, Long clusterId) {
        try {
            Long envId = getEnvId(key, clusterId);
            if (envId == null) {
                logger.info("{} {} clusterId:{}", ENV_NOT_EXIST, KeyParseUtil.getNamespace(key), clusterId);
                return;
            }

            Object obj = objectMapper.readValue(msg, Object.class);
            DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
            DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO();
            devopsEnvResourceDetailDTO.setMessage(msg);
            devopsEnvResourceDTO.setKind(KeyParseUtil.getResourceType(key));
            devopsEnvResourceDTO.setEnvId(envId);
            devopsEnvResourceDTO.setName(KeyParseUtil.getResourceName(key));
            devopsEnvResourceDTO.setReversion(
                    TypeUtil.objToLong(
                            ((LinkedHashMap) ((LinkedHashMap) obj).get(METADATA)).get(RESOURCE_VERSION).toString()));
            String releaseName = null;
            DevopsEnvResourceDTO oldDevopsEnvResourceDTO = null;
            AppServiceInstanceDTO appServiceInstanceDTO = null;
            ResourceType resourceType = ResourceType.forString(KeyParseUtil.getResourceType(key));

            if (resourceType == null) {
                resourceType = ResourceType.forString("MissType");
            }
            switch (resourceType) {
                case INGRESS:
                    oldDevopsEnvResourceDTO =
                            devopsEnvResourceService.baseQueryOptions(
                                    null,
                                    null,
                                    envId,
                                    KeyParseUtil.getResourceType(key),
                                    KeyParseUtil.getResourceName(key));
                    //升级0.11.0-0.12.0,资源表新增envId,修复以前的域名数据
                    if (oldDevopsEnvResourceDTO == null) {
                        oldDevopsEnvResourceDTO = devopsEnvResourceService.baseQueryOptions(
                                null,
                                null,
                                null,
                                KeyParseUtil.getResourceType(key),
                                KeyParseUtil.getResourceName(key));
                    }
                    saveOrUpdateResource(devopsEnvResourceDTO, oldDevopsEnvResourceDTO,
                            devopsEnvResourceDetailDTO, null);
                    break;
                case POD:
                    handlerUpdatePodMessage(key, msg, envId);
                    break;
                case SERVICE:
                    handleUpdateServiceMsg(key, envId, msg, devopsEnvResourceDTO);
                    break;
                case CONFIGMAP:
                    oldDevopsEnvResourceDTO =
                            devopsEnvResourceService.baseQueryOptions(
                                    null,
                                    null,
                                    envId,
                                    KeyParseUtil.getResourceType(key),
                                    KeyParseUtil.getResourceName(key));
                    saveOrUpdateResource(devopsEnvResourceDTO, oldDevopsEnvResourceDTO,
                            devopsEnvResourceDetailDTO, null);
                    break;
                case SECRET:
                    oldDevopsEnvResourceDTO = devopsEnvResourceService
                            .baseQueryOptions(null, null, envId, KeyParseUtil.getResourceType(key),
                                    KeyParseUtil.getResourceName(key));
                    saveOrUpdateResource(devopsEnvResourceDTO, oldDevopsEnvResourceDTO, devopsEnvResourceDetailDTO, null);
                    break;
                default:
                    releaseName = KeyParseUtil.getReleaseName(key);
                    if (releaseName != null) {
                        appServiceInstanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
                        if (appServiceInstanceDTO == null) {
                            return;
                        }
                        oldDevopsEnvResourceDTO =
                                devopsEnvResourceService.baseQueryOptions(
                                        appServiceInstanceDTO.getId(),
                                        resourceType.getType().equals(ResourceType.JOB.getType()) ? appServiceInstanceDTO.getCommandId() : null,
                                        envId,
                                        KeyParseUtil.getResourceType(key),
                                        KeyParseUtil.getResourceName(key));

                        if (oldDevopsEnvResourceDTO == null) {
                            oldDevopsEnvResourceDTO =
                                    devopsEnvResourceService.baseQueryOptions(
                                            appServiceInstanceDTO.getId(),
                                            resourceType.getType().equals(ResourceType.JOB.getType()) ? appServiceInstanceDTO.getCommandId() : null,
                                            null,
                                            KeyParseUtil.getResourceType(key),
                                            KeyParseUtil.getResourceName(key));
                        }
                        saveOrUpdateResource(devopsEnvResourceDTO, oldDevopsEnvResourceDTO, devopsEnvResourceDetailDTO, appServiceInstanceDTO);
                    }
                    break;
            }
        } catch (IOException e) {
            logger.info(e.toString());
        }
    }

    private void handleUpdateServiceMsg(String key, Long envId, String msg, DevopsEnvResourceDTO devopsEnvResourceDTO) {
        AppServiceInstanceDTO appServiceInstanceDTO;
        V1Service v1Service = json.deserialize(msg, V1Service.class);
        if (v1Service.getMetadata().getAnnotations() != null) {
            DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(v1Service.getMetadata().getName(), envId);
            if (devopsServiceDTO.getType().equals("LoadBalancer") &&
                    v1Service.getStatus() != null &&
                    v1Service.getStatus().getLoadBalancer() != null &&
                    !v1Service.getStatus().getLoadBalancer().getIngress().isEmpty()) {

                devopsServiceDTO.setLoadBalanceIp(v1Service.getStatus().getLoadBalancer().getIngress().get(0).getIp());
                List<PortMapVO> portMapVOS = getPortMapES(v1Service);

                devopsServiceDTO.setPorts(gson.toJson(portMapVOS));
                devopsServiceService.baseUpdate(devopsServiceDTO);
            }
            if (devopsServiceDTO.getType().equals("NodePort") && v1Service.getSpec().getPorts() != null) {
                List<PortMapVO> portMapVOS = getPortMapES(v1Service);
                devopsServiceDTO.setPorts(gson.toJson(portMapVOS));
                devopsServiceService.baseUpdate(devopsServiceDTO);

            }

            String releaseNames = v1Service.getMetadata().getAnnotations()
                    .get(CHOERODON_IO_NETWORK_SERVICE_INSTANCES);
            if (releaseNames != null) {
                String[] releases = releaseNames.split("\\+");
                List<Long> beforeInstanceIdS = devopsEnvResourceService.baseListByEnvAndType(envId, SERVICE_KIND)
                        .stream()
                        .filter(result -> result.getName().equals(v1Service.getMetadata().getName()))
                        .map(DevopsEnvResourceDTO::getInstanceId)
                        .collect(Collectors.toList());
                List<Long> afterInstanceIds = new ArrayList<>();
                for (String release : releases) {
                    appServiceInstanceDTO = appServiceInstanceService
                            .baseQueryByCodeAndEnv(release, envId);
                    if (appServiceInstanceDTO != null) {
                        DevopsEnvResourceDTO oldDevopsEnvResourceDTO =
                                devopsEnvResourceService.baseQueryOptions(
                                        appServiceInstanceDTO.getId(),
                                        null,
                                        null,
                                        KeyParseUtil.getResourceType(key),
                                        KeyParseUtil.getResourceName(key));
                        DevopsEnvResourceDetailDTO newDevopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO();
                        newDevopsEnvResourceDetailDTO.setMessage(msg);
                        saveOrUpdateResource(devopsEnvResourceDTO, oldDevopsEnvResourceDTO,
                                newDevopsEnvResourceDetailDTO, appServiceInstanceDTO);
                        afterInstanceIds.add(appServiceInstanceDTO.getId());
                    }
                }
                //网络更新实例删除网络以前实例网络关联的resource
                for (Long instanceId : beforeInstanceIdS) {
                    if (!afterInstanceIds.contains(instanceId)) {
                        devopsEnvResourceService.deleteByKindAndNameAndInstanceId(SERVICE_KIND, v1Service.getMetadata().getName(), instanceId);
                    }
                }
            }
        }
    }

    private List<PortMapVO> getPortMapES(V1Service v1Service) {
        return v1Service.getSpec().getPorts().stream().map(v1ServicePort -> {
            PortMapVO portMapVO = new PortMapVO();
            portMapVO.setPort(TypeUtil.objToLong(v1ServicePort.getPort()));
            portMapVO.setTargetPort(TypeUtil.objToString(v1ServicePort.getTargetPort()));
            portMapVO.setNodePort(TypeUtil.objToLong(v1ServicePort.getNodePort()));
            portMapVO.setProtocol(v1ServicePort.getProtocol());
            portMapVO.setName(v1ServicePort.getName());
            return portMapVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resourceDelete(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        if (KeyParseUtil.getResourceType(key).equals(ResourceType.JOB.getType())) {
            return;
        }
        if (KeyParseUtil.getResourceType(key).equals(ResourceType.POD.getType())) {
            String podName = KeyParseUtil.getResourceName(key);
            String podNameSpace = KeyParseUtil.getNamespace(key);
            devopsEnvPodService.baseDeleteByName(podName, podNameSpace);
        }

        devopsEnvResourceService.deleteByEnvIdAndKindAndName(
                envId,
                KeyParseUtil.getResourceType(key),
                KeyParseUtil.getResourceName(key));

    }

    @Override
    public void helmJobLog(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(KeyParseUtil.getReleaseName(key), envId);
        if (appServiceInstanceDTO != null) {
            // 删除实例历史日志记录
            devopsEnvCommandLogService.baseDeleteByInstanceId(appServiceInstanceDTO.getId());
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                    .baseQueryByObject(ObjectType.INSTANCE.getType(), appServiceInstanceDTO.getId());
            if (devopsEnvCommandDTO != null) {
                DevopsEnvCommandLogDTO devopsEnvCommandLogDTO = new DevopsEnvCommandLogDTO();
                devopsEnvCommandLogDTO.setCommandId(devopsEnvCommandDTO.getId());
                devopsEnvCommandLogDTO.setLog(msg);
                devopsEnvCommandLogService.baseCreate(devopsEnvCommandLogDTO);
            }
        }
    }

    @Override
    public void updateInstanceStatus(String key, String releaseName, Long clusterId, String instanceStatus, String commandStatus, String msg) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        AppServiceInstanceDTO instanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
        if (instanceDTO != null) {
            instanceDTO.setStatus(instanceStatus);
            appServiceInstanceService.baseUpdate(instanceDTO);
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                    .baseQueryByObject(ObjectType.INSTANCE.getType(), instanceDTO.getId());
            devopsEnvCommandDTO.setStatus(commandStatus);
            devopsEnvCommandDTO.setError(msg);
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        }
    }

    @Override
    public void handlerDomainCreateMessage(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        V1beta1Ingress ingress = json.deserialize(msg, V1beta1Ingress.class);
        DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO();
        devopsEnvResourceDetailDTO.setMessage(msg);
        devopsEnvResourceDTO.setKind(KeyParseUtil.getResourceType(key));
        devopsEnvResourceDTO.setName(KeyParseUtil.getResourceName(key));
        devopsEnvResourceDTO.setEnvId(envId);
        devopsEnvResourceDTO.setReversion(TypeUtil.objToLong(ingress.getMetadata().getResourceVersion()));
        DevopsEnvResourceDTO oldDevopsEnvResourceDTO =
                devopsEnvResourceService.baseQueryOptions(
                        null,
                        null,
                        envId,
                        KeyParseUtil.getResourceType(key),
                        KeyParseUtil.getResourceName(key));
        if (oldDevopsEnvResourceDTO == null) {
            oldDevopsEnvResourceDTO =
                    devopsEnvResourceService.baseQueryOptions(
                            null,
                            null,
                            null,
                            KeyParseUtil.getResourceType(key),
                            KeyParseUtil.getResourceName(key));
        }
        saveOrUpdateResource(devopsEnvResourceDTO, oldDevopsEnvResourceDTO, devopsEnvResourceDetailDTO, null);
        String ingressName = ingress.getMetadata().getName();
        devopsIngressService.baseUpdateStatus(envId, ingressName, IngressStatus.RUNNING.getStatus());
    }

    @Override
    public void helmUpgradeJobInfo(String key, String msg, Long clusterId) {
        helmInstallJobInfo(key, msg, clusterId);
    }

    @Override
    public void helmUpgradeResourceInfo(String key, String msg, Long clusterId) {
        helmInstallResourceInfo(key, msg, clusterId);
    }


    @Override
    public void helmReleaseDeleteFail(String key, String msg, Long clusterId) {

        updateInstanceStatus(key, KeyParseUtil.getReleaseName(key),
                clusterId,
                InstanceStatus.DELETED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaseStartFail(String key, String msg, Long clusterId) {
        updateInstanceStatus(
                key,
                KeyParseUtil.getReleaseName(key),
                clusterId,
                InstanceStatus.STOPPED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaseRollBackFail(String key, String msg) {
        logger.info(key);
    }

    @Override
    public void helmReleaseInstallFail(String key, String msg, Long clusterId) {
        updateInstanceStatus(
                key, KeyParseUtil.getReleaseName(key),
                clusterId,
                InstanceStatus.FAILED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaseUpgradeFail(String key, String msg, Long clusterId) {

        updateInstanceStatus(key, KeyParseUtil.getReleaseName(key),
                clusterId,
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaseStopFail(String key, String msg, Long clusterId) {
        updateInstanceStatus(key, KeyParseUtil.getReleaseName(key),
                clusterId,
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);

    }


    @Override
    public void commandNotSend(Long commandId, String msg) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(commandId);
        devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
        devopsEnvCommandDTO.setError(msg);
        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        if (devopsEnvCommandDTO.getCommandType().equals(CommandType.CREATE.getType())) {
            if (devopsEnvCommandDTO.getObject().equals(ObjectType.INSTANCE.getType())) {
                AppServiceInstanceDTO appServiceInstanceDTO =
                        appServiceInstanceService.baseQuery(devopsEnvCommandDTO.getObjectId());
                appServiceInstanceDTO.setStatus(InstanceStatus.FAILED.getStatus());
                appServiceInstanceService.baseUpdate(appServiceInstanceDTO);
            } else if (devopsEnvCommandDTO.getObject().equals(ObjectType.SERVICE.getType())) {
                DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQuery(devopsEnvCommandDTO.getObjectId());
                devopsServiceDTO.setStatus(ServiceStatus.FAILED.getStatus());
                devopsServiceService.baseUpdate(devopsServiceDTO);
            } else if (devopsEnvCommandDTO.getObject().equals(ObjectType.INGRESS.getType())) {
                DevopsIngressDTO ingress = devopsIngressService.baseQuery(devopsEnvCommandDTO.getObjectId());
                devopsIngressService.baseUpdateStatus(ingress.getEnvId(), ingress.getName(), IngressStatus.FAILED.getStatus());
            }
            //其他资源也要加上更新状态的逻辑，todo
        }
    }

    @Override
    public void resourceSync(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        ResourceSyncPayloadDTO resourceSyncPayloadDTO = JSONArray.parseObject(msg, ResourceSyncPayloadDTO.class);
        ResourceType resourceType = ResourceType.forString(resourceSyncPayloadDTO.getResourceType());
        List<DevopsEnvResourceDTO> devopsEnvResourceDTOS;
        if (resourceType == null) {
            resourceType = ResourceType.forString("MissType");
        }
        if (resourceSyncPayloadDTO.getResources() == null) {
            return;
        }
        switch (resourceType) {
            case POD:
                devopsEnvResourceDTOS = devopsEnvResourceService
                        .baseListByEnvAndType(envId, ResourceType.POD.getType());
                if (!devopsEnvResourceDTOS.isEmpty()) {
                    List<String> podNames = Arrays.asList(resourceSyncPayloadDTO.getResources());
                    devopsEnvResourceDTOS.stream()
                            .filter(devopsEnvResourceDTO -> !podNames.contains(devopsEnvResourceDTO.getName()))
                            .forEach(devopsEnvResourceDTO -> {
                                devopsEnvResourceService.deleteByEnvIdAndKindAndName(
                                        envId, ResourceType.POD.getType(), devopsEnvResourceDTO.getName());
                                devopsEnvPodService.baseDeleteByName(
                                        devopsEnvResourceDTO.getName(), KeyParseUtil.getValue(key, "env"));
                            });
                }
                break;
            case DEPLOYMENT:
                devopsEnvResourceDTOS = devopsEnvResourceService
                        .baseListByEnvAndType(envId, ResourceType.DEPLOYMENT.getType());
                if (!devopsEnvResourceDTOS.isEmpty()) {
                    List<String> deploymentNames = Arrays.asList(resourceSyncPayloadDTO.getResources());
                    devopsEnvResourceDTOS.stream()
                            .filter(devopsEnvResourceDTO -> !deploymentNames.contains(devopsEnvResourceDTO.getName()))
                            .forEach(devopsEnvResourceDTO ->
                                    devopsEnvResourceService.deleteByEnvIdAndKindAndName(
                                            envId, ResourceType.DEPLOYMENT.getType(), devopsEnvResourceDTO.getName()));
                }
                break;
            case REPLICASET:
                devopsEnvResourceDTOS = devopsEnvResourceService
                        .baseListByEnvAndType(envId, ResourceType.REPLICASET.getType());
                if (!devopsEnvResourceDTOS.isEmpty()) {
                    List<String> replicaSetNames = Arrays.asList(resourceSyncPayloadDTO.getResources());
                    devopsEnvResourceDTOS.stream()
                            .filter(devopsEnvResourceDTO -> !replicaSetNames.contains(devopsEnvResourceDTO.getName()))
                            .forEach(devopsEnvResourceDTO ->
                                    devopsEnvResourceService.deleteByEnvIdAndKindAndName(
                                            envId, ResourceType.REPLICASET.getType(), devopsEnvResourceDTO.getName()));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void helmJobEvent(String msg) {
        Event event = JSONArray.parseObject(msg, Event.class);
        if (event.getInvolvedObject().getKind().equals(ResourceType.POD.getType())) {
            event.getInvolvedObject().setKind(ResourceType.JOB.getType());
            event.getInvolvedObject().setName(
                    event.getInvolvedObject().getName()
                            .substring(0, event.getInvolvedObject().getName().lastIndexOf('-')));
            insertDevopsCommandEvent(event, ResourceType.JOB.getType());
        }
    }

    @Override
    public void helmPodEvent(String msg) {
        Event event = JSONArray.parseObject(msg, Event.class);
        insertDevopsCommandEvent(event, ResourceType.POD.getType());
    }

    @Override
    public void gitOpsSyncEvent(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        logger.info("env {} receive git ops msg :\n{}", envId, msg);
        GitOpsSyncDTO gitOpsSyncDTO = JSONArray.parseObject(msg, GitOpsSyncDTO.class);
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        DevopsEnvCommitDTO agentSyncCommitDTO = devopsEnvCommitService.baseQuery(devopsEnvironmentDTO.getAgentSyncCommit());
        if (agentSyncCommitDTO != null && agentSyncCommitDTO.getCommitSha().equals(gitOpsSyncDTO.getMetadata().getCommit())) {
            return;
        }
        DevopsEnvCommitDTO devopsEnvCommitDTO = devopsEnvCommitService.baseQueryByEnvIdAndCommit(envId, gitOpsSyncDTO.getMetadata().getCommit());
        if (devopsEnvCommitDTO == null) {
            return;
        }
        devopsEnvironmentDTO.setAgentSyncCommit(devopsEnvCommitDTO.getId());
        devopsEnvironmentService.baseUpdateAgentSyncEnvCommit(devopsEnvironmentDTO);
        if (gitOpsSyncDTO.getResourceIDs() == null) {
            return;
        }
        if (gitOpsSyncDTO.getResourceIDs().isEmpty()) {
            return;
        }
        List<DevopsEnvFileErrorDTO> errorDevopsFiles = getEnvFileErrors(envId, gitOpsSyncDTO, devopsEnvironmentDTO);

        gitOpsSyncDTO.getMetadata().getFilesCommit().forEach(fileCommit -> {
            if (fileCommit.getFile().endsWith(".yaml") || fileCommit.getFile().endsWith("yml")) {
                DevopsEnvFileDTO devopsEnvFileDTO = devopsEnvFileService.baseQueryByEnvAndPath(devopsEnvironmentDTO.getId(), fileCommit.getFile());
                devopsEnvFileDTO.setAgentCommit(fileCommit.getCommit());
                devopsEnvFileService.baseUpdate(devopsEnvFileDTO);
            }
        });
        gitOpsSyncDTO.getMetadata().getResourceCommits()
                .forEach(resourceCommitVO -> {
                    String[] objects = resourceCommitVO.getResourceId().split("/");
                    switch (objects[0]) {
                        case C7NHELMRELEASE_KIND:
                            syncC7nHelmRelease(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        case INGRESS_KIND:
                            syncIngress(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        case SERVICE_KIND:
                            syncService(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        case CERTIFICATE_KIND:
                            syncCetificate(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        case CONFIGMAP_KIND:
                            syncConfigMap(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        case SECRET_KIND:
                            syncSecret(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        default:
                            syncCustom(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                    }
                });
    }

    private void syncSecret(Long envId, List<DevopsEnvFileErrorDTO> envFileErrorFiles, ResourceCommitVO resourceCommitVO,
                            String[] objects) {

        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        DevopsSecretDTO devopsSecretDTO = devopsSecretService.baseQueryByEnvIdAndName(envId, objects[1]);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsSecretDTO.getId(), ObjectType.SECRET.getType());
        updateEnvCommandStatus(resourceCommitVO, devopsSecretDTO.getCommandId(), devopsEnvFileResourceDTO,
                SECRET_KIND, devopsSecretDTO.getName(), CommandStatus.SUCCESS.getStatus(), envFileErrorFiles);
    }


    private void syncCustom(Long envId, List<DevopsEnvFileErrorDTO> envFileErrorFiles, ResourceCommitVO resourceCommitVO,
                            String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, objects[0], objects[1]);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsCustomizeResourceDTO.getId(), ObjectType.CUSTOM.getType());
        updateEnvCommandStatus(resourceCommitVO, devopsCustomizeResourceDTO.getCommandId(), devopsEnvFileResourceDTO,
                objects[0], devopsCustomizeResourceDTO.getName(), CommandStatus.SUCCESS.getStatus(), envFileErrorFiles);

    }

    private void syncCetificate(Long envId, List<DevopsEnvFileErrorDTO> errorDevopsFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        CertificationDTO certificationDTO = certificationService
                .baseQueryByEnvAndName(envId, objects[1]);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(
                        envId, certificationDTO.getId(), "Certificate");
        if (updateEnvCommandStatus(resourceCommitVO, certificationDTO.getCommandId(),
                devopsEnvFileResourceDTO, CERTIFICATE_KIND, certificationDTO.getName(),
                null, errorDevopsFiles)) {
            certificationDTO.setStatus(CertificationStatus.FAILED.getStatus());
        } else {
            certificationDTO.setStatus(CertificationStatus.APPLYING.getStatus());
        }
        certificationService.baseUpdateStatus(certificationDTO);
    }

    private void syncService(Long envId, List<DevopsEnvFileErrorDTO> errorDevopsFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        DevopsServiceDTO devopsServiceDTO = devopsServiceService
                .baseQueryByNameAndEnvId(objects[1], envId);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsServiceDTO.getId(), "Service");
        if (updateEnvCommandStatus(resourceCommitVO, devopsServiceDTO.getCommandId(),
                devopsEnvFileResourceDTO, SERVICE_KIND, devopsServiceDTO.getName(), CommandStatus.SUCCESS.getStatus(), errorDevopsFiles)) {
            devopsServiceDTO.setStatus(ServiceStatus.FAILED.getStatus());
        } else {
            devopsServiceDTO.setStatus(ServiceStatus.RUNNING.getStatus());
        }
        devopsServiceService.baseUpdate(devopsServiceDTO);
    }

    private void syncService(DevopsServiceDTO devopsServiceDTO, String msg, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1Service v1Service = json.deserialize(msg, V1Service.class);
        Map<String, String> lab = v1Service.getMetadata().getLabels();
        if (lab.get(SERVICE_LABLE) != null && lab.get(SERVICE_LABLE).equals(SERVICE_KIND)) {
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(
                    appServiceInstanceDTO.getEnvId());
            if (devopsServiceService.baseQueryByNameAndEnvId(
                    v1Service.getMetadata().getName(), devopsEnvironmentDTO.getId()) == null) {
                devopsServiceDTO.setEnvId(devopsEnvironmentDTO.getId());
                devopsServiceDTO.setAppServiceId(appServiceInstanceDTO.getAppServiceId());
                devopsServiceDTO.setName(v1Service.getMetadata().getName());
                devopsServiceDTO.setType(v1Service.getSpec().getType());
                devopsServiceDTO.setStatus(ServiceStatus.RUNNING.getStatus());
                devopsServiceDTO.setPorts(gson.fromJson(
                        gson.toJson(v1Service.getSpec().getPorts()),
                        new TypeToken<ArrayList<PortMapVO>>() {
                        }.getType()));
                if (v1Service.getSpec().getExternalIPs() != null) {
                    devopsServiceDTO.setExternalIp(String.join(",", v1Service.getSpec().getExternalIPs()));
                }
                devopsServiceDTO.setLabels(json.serialize(v1Service.getMetadata().getLabels()));
                devopsServiceDTO.setAnnotations(json.serialize(v1Service.getMetadata().getAnnotations()));
                devopsServiceDTO.setId(devopsServiceService.baseCreate(devopsServiceDTO).getId());

                DevopsServiceInstanceDTO devopsServiceInstanceDTO = devopsServiceInstanceService
                        .baseQueryByOptions(devopsServiceDTO.getId(), appServiceInstanceDTO.getId());
                if (devopsServiceInstanceDTO == null) {
                    devopsServiceInstanceDTO = new DevopsServiceInstanceDTO();
                    devopsServiceInstanceDTO.setServiceId(devopsServiceDTO.getId());
                    devopsServiceInstanceDTO.setInstanceId(appServiceInstanceDTO.getId());
                    devopsServiceInstanceService.baseCreate(devopsServiceInstanceDTO);
                }

                DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
                devopsEnvCommandDTO.setObject(ObjectType.SERVICE.getType());
                devopsEnvCommandDTO.setObjectId(devopsServiceDTO.getId());
                devopsEnvCommandDTO.setCommandType(CommandType.CREATE.getType());
                devopsEnvCommandDTO.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsEnvCommandService.baseCreate(devopsEnvCommandDTO);
            }
        } else {
            devopsEnvResourceService.deleteByEnvIdAndKindAndName(appServiceInstanceDTO.getEnvId(),
                    ResourceType.SERVICE.getType(), v1Service.getMetadata().getName());
        }
    }

    private void syncIngress(Long envId, List<DevopsEnvFileErrorDTO> errorDevopsFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        DevopsIngressDTO devopsIngressDTO = devopsIngressService
                .baseCheckByEnvAndName(envId, objects[1]);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsIngressDTO.getId(), "Ingress");
        if (updateEnvCommandStatus(resourceCommitVO, devopsIngressDTO.getCommandId(),
                devopsEnvFileResourceDTO, INGRESS_KIND, devopsIngressDTO.getName(), CommandStatus.SUCCESS.getStatus(), errorDevopsFiles)) {
            devopsIngressService.baseUpdateStatus(envId, devopsIngressDTO.getName(), IngressStatus.FAILED.getStatus());
        } else {
            devopsIngressService.baseUpdateStatus(envId, devopsIngressDTO.getName(), IngressStatus.RUNNING.getStatus());
        }
    }

    private void syncC7nHelmRelease(Long envId, List<DevopsEnvFileErrorDTO> errorDevopsFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService
                .baseQueryByCodeAndEnv(objects[1], envId);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, appServiceInstanceDTO.getId(), "C7NHelmRelease");
        if (updateEnvCommandStatus(resourceCommitVO, appServiceInstanceDTO.getCommandId(),
                devopsEnvFileResourceDTO, C7NHELMRELEASE_KIND, appServiceInstanceDTO.getCode(), null, errorDevopsFiles)) {
            appServiceInstanceDTO.setStatus(InstanceStatus.FAILED.getStatus());
            appServiceInstanceService.baseUpdate(appServiceInstanceDTO);
        }
    }

    private void syncConfigMap(Long envId, List<DevopsEnvFileErrorDTO> errorDevopsFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService
                .baseQueryByEnvIdAndName(envId, objects[1]);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsConfigMapDTO.getId(), "ConfigMap");
        updateEnvCommandStatus(resourceCommitVO, devopsConfigMapDTO.getCommandId(),
                devopsEnvFileResourceDTO, CONFIGMAP_KIND, devopsConfigMapDTO.getName(), CommandStatus.SUCCESS.getStatus(), errorDevopsFiles);
    }

    private List<DevopsEnvFileErrorDTO> getEnvFileErrors(Long envId, GitOpsSyncDTO gitOpsSyncDTO, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        List<DevopsEnvFileErrorDTO> errorDevopsFiles = new ArrayList<>();
        if (gitOpsSyncDTO.getMetadata().getErrors() != null) {
            gitOpsSyncDTO.getMetadata().getErrors().stream().forEach(error -> {
                DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = devopsEnvFileErrorService.baseQueryByEnvIdAndFilePath(envId, error.getPath());
                if (devopsEnvFileErrorDTO == null) {
                    devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO();
                    devopsEnvFileErrorDTO.setCommit(error.getCommit());
                    devopsEnvFileErrorDTO.setError(error.getError());
                    devopsEnvFileErrorDTO.setFilePath(error.getPath());
                    devopsEnvFileErrorDTO.setEnvId(devopsEnvironmentDTO.getId());
                    devopsEnvFileErrorService.baseCreateOrUpdate(devopsEnvFileErrorDTO);
                    devopsEnvFileErrorDTO.setResource(error.getId());
                } else {
                    devopsEnvFileErrorDTO.setError(devopsEnvFileErrorDTO.getError() + error.getError());
                    devopsEnvFileErrorDTO = devopsEnvFileErrorService.baseCreateOrUpdate(devopsEnvFileErrorDTO);
                    devopsEnvFileErrorDTO.setResource(error.getId());
                }
                errorDevopsFiles.add(devopsEnvFileErrorDTO);
            });
        }
        return errorDevopsFiles;
    }

    private boolean updateEnvCommandStatus(ResourceCommitVO resourceCommitVO, Long commandId,
                                           DevopsEnvFileResourceDTO devopsEnvFileResourceDTO,
                                           String objectType, String objectName, String passStatus,
                                           List<DevopsEnvFileErrorDTO> envFileErrorDTOS) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(commandId);
        if (resourceCommitVO.getCommit().equals(devopsEnvCommandDTO.getSha()) && passStatus != null) {
            devopsEnvCommandDTO.setStatus(passStatus);
        }
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = devopsEnvFileErrorService
                .baseQueryByEnvIdAndFilePath(devopsEnvFileResourceDTO.getEnvId(), devopsEnvFileResourceDTO.getFilePath());
        if (devopsEnvFileErrorDTO != null) {
            List<DevopsEnvFileErrorDTO> devopsEnvFileErrorDTOS = envFileErrorDTOS.stream().filter(devopsEnvFileErrorDTO1 -> devopsEnvFileErrorDTO1.getId().equals(devopsEnvFileErrorDTO.getId())).collect(Collectors.toList());
            if (!devopsEnvFileErrorDTOS.isEmpty()) {
                String[] objects = devopsEnvFileErrorDTOS.get(0).getResource().split("/");
                if (objects[0].equals(objectType) && objects[1].equals(objectName)) {
                    devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                    devopsEnvCommandDTO.setError(devopsEnvFileErrorDTO.getError());
                    devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
                    return true;
                }
            }
        }
        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        return false;
    }

    private void saveOrUpdateResource(DevopsEnvResourceDTO devopsEnvResourceDTO,
                                      DevopsEnvResourceDTO oldDevopsEnvResourceDTO,
                                      DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO,
                                      AppServiceInstanceDTO appServiceInstanceDTO) {
        if (appServiceInstanceDTO != null) {
            devopsEnvResourceDTO.setInstanceId(appServiceInstanceDTO.getId());
            devopsEnvResourceDTO.setCommandId(appServiceInstanceDTO.getCommandId());
        }
        if (oldDevopsEnvResourceDTO == null) {
            devopsEnvResourceDTO.setResourceDetailId(
                    devopsEnvResourceDetailService.baseCreate(devopsEnvResourceDetailDTO).getId());
            devopsEnvResourceService.baseCreate(devopsEnvResourceDTO);
            return;
        }
        if (oldDevopsEnvResourceDTO.getReversion() == null) {
            oldDevopsEnvResourceDTO.setReversion(0L);
        }
        if (devopsEnvResourceDTO.getReversion() == null) {
            devopsEnvResourceDTO.setReversion(0L);
        }
        if (appServiceInstanceDTO != null) {
            oldDevopsEnvResourceDTO.setDevopsEnvCommandId(appServiceInstanceDTO.getCommandId());
            oldDevopsEnvResourceDTO.setInstanceId(devopsEnvResourceDTO.getInstanceId());
        }
        if (devopsEnvResourceDTO.getEnvId() != null) {
            oldDevopsEnvResourceDTO.setEnvId(devopsEnvResourceDTO.getEnvId());

            devopsEnvResourceService.baseUpdate(oldDevopsEnvResourceDTO);
        }
        if (!oldDevopsEnvResourceDTO.getReversion().equals(devopsEnvResourceDTO.getReversion())) {
            oldDevopsEnvResourceDTO.setReversion(devopsEnvResourceDTO.getReversion());
            devopsEnvResourceDetailDTO.setId(oldDevopsEnvResourceDTO.getResourceDetailId());
            devopsEnvResourceService.baseUpdate(oldDevopsEnvResourceDTO);
            devopsEnvResourceDetailService.baseUpdate(devopsEnvResourceDetailDTO);
        }

    }

    private void installResource(List<Resource> resources, AppServiceInstanceDTO appServiceInstanceDTO) {
        try {
            for (Resource resource : resources) {
                Long instanceId = appServiceInstanceDTO.getId();
                if (resource.getKind().equals(ResourceType.INGRESS.getType())) {
                    instanceId = null;
                }
                DevopsEnvResourceDTO oldDevopsEnvResourceDTO =
                        devopsEnvResourceService.baseQueryOptions(
                                instanceId,
                                null, null,
                                resource.getKind(),
                                resource.getName());
                DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO();
                devopsEnvResourceDetailDTO.setMessage(resource.getObject());
                DevopsEnvResourceDTO devopsEnvResourceDTO =
                        new DevopsEnvResourceDTO();
                devopsEnvResourceDTO.setKind(resource.getKind());
                devopsEnvResourceDTO.setName(resource.getName());
                devopsEnvResourceDTO.setEnvId(appServiceInstanceDTO.getEnvId());
                JSONObject jsonResult = JSONObject.parseObject(JSONObject.parseObject(resource.getObject())
                        .get(METADATA).toString());
                devopsEnvResourceDTO.setReversion(
                        TypeUtil.objToLong(jsonResult.get(RESOURCE_VERSION).toString()));
                saveOrUpdateResource(
                        devopsEnvResourceDTO,
                        oldDevopsEnvResourceDTO,
                        devopsEnvResourceDetailDTO,
                        appServiceInstanceDTO);
                if (resource.getKind().equals(ResourceType.POD.getType())) {
                    syncPod(resource.getObject(), appServiceInstanceDTO);
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    private void syncPod(String msg, AppServiceInstanceDTO appServiceInstanceDTO) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);
        String status = K8sUtil.changePodStatus(v1Pod);
        String resourceVersion = v1Pod.getMetadata().getResourceVersion();

        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        devopsEnvPodDTO.setName(v1Pod.getMetadata().getName());
        devopsEnvPodDTO.setIp(v1Pod.getStatus().getPodIP());
        devopsEnvPodDTO.setStatus(status);
        devopsEnvPodDTO.setResourceVersion(resourceVersion);
        devopsEnvPodDTO.setNamespace(v1Pod.getMetadata().getNamespace());
        devopsEnvPodDTO.setReady(getReadyValue(status, v1Pod));
        devopsEnvPodDTO.setInstanceId(appServiceInstanceDTO.getId());
        devopsEnvPodDTO.setNodeName(v1Pod.getSpec().getNodeName());
        devopsEnvPodDTO.setRestartCount(K8sUtil.getRestartCountForPod(v1Pod));
        devopsEnvPodService.baseCreate(devopsEnvPodDTO);
    }

    private void insertDevopsCommandEvent(Event event, String type) {
        DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService
                .baseQueryByKindAndName(event.getInvolvedObject().getKind(), event.getInvolvedObject().getName());
        try {
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                    .baseQueryByObject(ObjectType.INSTANCE.getType(), devopsEnvResourceDTO.getInstanceId());
            // 删除实例事件记录
            devopsCommandEventService.baseDeletePreInstanceCommandEvent(devopsEnvCommandDTO.getObjectId());
            DevopsCommandEventDTO devopsCommandEventDTO = new DevopsCommandEventDTO();
            devopsCommandEventDTO.setEventCreationTime(event.getMetadata().getCreationTimestamp());
            devopsCommandEventDTO.setMessage(event.getMessage());
            devopsCommandEventDTO.setName(event.getInvolvedObject().getName());
            devopsCommandEventDTO.setCommandId(devopsEnvCommandDTO.getId());
            devopsCommandEventDTO.setType(type);
            devopsCommandEventService.baseCreate(devopsCommandEventDTO);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }


    @Override
    public List<AppServiceDTO> getApplication(String appServiceName, Long projectId, Long orgId) {
        List<AppServiceDTO> applications = new ArrayList<>();
        AppServiceDTO applicationDTO = applicationService
                .baseQueryByCode(appServiceName, projectId);
        if (applicationDTO != null) {
            applications.add(applicationDTO);
        }
        List<AppServiceDTO> applicationDTOS = applicationService.baseListByCode(appServiceName);
        List<AppServiceDTO> applicationList = applicationDTOS.stream()
                .filter(result ->
                        baseServiceClientOperator.queryIamProjectById(result.getAppId()).getOrganizationId().equals(orgId))
                .collect(Collectors.toList());
        applications.addAll(findAppInAppMarket(applicationDTOS, applicationList));
        return applications;
    }

    private List<AppServiceDTO> findAppInAppMarket(List<AppServiceDTO> applicationDTOS, List<AppServiceDTO> applicationList) {
        List<AppServiceDTO> applications = new ArrayList<>();
//        if (!applicationList.isEmpty()) {
//            applicationList.forEach(applicationDTO -> {
//                if (applicationShareService.baseCountByAppId(applicationDTO.getId()) != 0) {
//                    applications.add(applicationDTO);
//                }
//            });
//        }
//        if (!applicationDTOS.isEmpty()) {
//            applicationDTOS.forEach(applicationDTO -> {
//                ApplicationShareRuleDTO applicationShareDTO =
//                        applicationShareService.baseQueryByAppId(applicationDTO.getId());
//                if (applicationShareDTO != null
//                        || !applicationShareDTO.getPublishLevel().equals(PUBLIC)) {
//                    applications.add(applicationDTO);
//                }
//            });
//        }
        return applications;
    }

    @Override
    public void resourceStatusSyncEvent(String key, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        logger.info("sync command status!");
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        List<Command> commands = new ArrayList<>();
        getCommands(envId, commands);

        agentCommandService.gitopsSyncCommandStatus(clusterId, devopsEnvironmentDTO.getCode(), envId, commands);

    }


    private void getCommands(Long envId, List<Command> commands) {
        List<Command> removeCommands = new ArrayList<>();
        appServiceInstanceService.baseListByEnvId(envId).stream().forEach(applicationInstanceDTO -> {
            Long commandId = applicationInstanceDTO.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(applicationInstanceDTO.getCommandId());
                command.setResourceType(INSTANCE_KIND);
                command.setResourceName(applicationInstanceDTO.getCode());
                commands.add(command);
            }
        });
        devopsServiceService.baseListByEnvId(envId).stream().forEach(devopsServiceDTO -> {
            Long commandId = devopsServiceDTO.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(devopsServiceDTO.getCommandId());
                command.setResourceType(SERVICE_KIND);
                command.setResourceName(devopsServiceDTO.getName());
                commands.add(command);
            }
        });
        devopsIngressService.baseListByEnvId(envId).stream().forEach(devopsIngressDTO -> {
            Long commandId = devopsIngressDTO.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(devopsIngressDTO.getCommandId());
                command.setResourceType(INGRESS_KIND);
                command.setResourceName(devopsIngressDTO.getName());
                commands.add(command);
            }
        });
        certificationService.baseListByEnvId(envId).stream().forEach(certificationDTO -> {
            Long commandId = certificationDTO.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(certificationDTO.getCommandId());
                command.setResourceType(CERTIFICATE_KIND);
                command.setResourceName(certificationDTO.getName());
                commands.add(command);
            }
        });
        for (DevopsConfigMapDTO devopsConfigMapDTO : devopsConfigMapService.baseListByEnv(envId)) {
            Long commandId = devopsConfigMapDTO.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(commandId);
                command.setResourceType(CONFIGMAP_KIND);
                command.setResourceName(devopsConfigMapDTO.getName());
                commands.add(command);
            }
        }
        devopsSecretService.baseListByEnv(envId).forEach(devopsSecretDTO -> {
            Long commandId = devopsSecretDTO.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(commandId);
                command.setResourceType(SECRET_KIND);
                command.setResourceName(devopsSecretDTO.getName());
                commands.add(command);
            }
        });
        Date d = new Date();
        if (!commands.isEmpty()) {
            commands.stream().forEach(command -> {
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(command.getId());
                command.setCommit(devopsEnvCommandDTO.getSha());
                if (!CommandStatus.OPERATING.getStatus().equals(devopsEnvCommandDTO.getStatus()) || (CommandStatus.OPERATING.getStatus().equals(devopsEnvCommandDTO.getStatus()) && d.getTime() - devopsEnvCommandDTO.getLastUpdateDate().getTime() <= 180000)) {
                    removeCommands.add(command);
                }
            });
            commands.removeAll(removeCommands);
        }
    }


    @Override
    public void resourceStatusSync(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        logger.info("sync command status result: {}.", msg);
        List<Command> commands = JSONArray.parseArray(msg, Command.class);
        List<Command> oldCommands = new ArrayList<>();
        getCommands(envId, oldCommands);
        if (!oldCommands.isEmpty()) {
            oldCommands.stream().filter(oldCommand -> oldCommand.getId() != null).forEach(command ->
                    commands.stream().filter(command1 -> command1.getId() != null && command1.getId().equals(command.getId())).forEach(command1 -> {
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(command.getId());
                        if (command1.getCommit() != null && command.getCommit() != null && command.getCommit().equals(command1.getCommit())) {
                            devopsEnvCommandDTO.setStatus(CommandStatus.SUCCESS.getStatus());
                            updateResourceStatus(envId, devopsEnvCommandDTO, InstanceStatus.RUNNING, ServiceStatus.RUNNING, IngressStatus.RUNNING, CertificationStatus.ACTIVE);
                        } else {
                            devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                            devopsEnvCommandDTO.setError("The deploy is time out!");
                            updateResourceStatus(envId, devopsEnvCommandDTO, InstanceStatus.FAILED, ServiceStatus.FAILED, IngressStatus.FAILED, CertificationStatus.FAILED);
                        }
                        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
                    })
            );
        }
    }

    @Override
    public void handlerServiceCreateMessage(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(
                KeyParseUtil.getResourceName(key), envId);
        try {
            V1Service v1Service = json.deserialize(msg, V1Service.class);
            String releaseNames = v1Service.getMetadata().getAnnotations()
                    .get(CHOERODON_IO_NETWORK_SERVICE_INSTANCES);
            String[] releases = releaseNames.split("\\+");
            DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
            DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO();
            devopsEnvResourceDetailDTO.setMessage(msg);
            devopsEnvResourceDTO.setKind(KeyParseUtil.getResourceType(key));
            devopsEnvResourceDTO.setName(v1Service.getMetadata().getName());
            devopsEnvResourceDTO.setEnvId(envId);
            devopsEnvResourceDTO.setReversion(TypeUtil.objToLong(v1Service.getMetadata().getResourceVersion()));
            for (String release : releases) {
                AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService
                        .baseQueryByCodeAndEnv(release, envId);

                DevopsEnvResourceDTO oldDevopsEnvResourceDTO = devopsEnvResourceService
                        .baseQueryOptions(
                                appServiceInstanceDTO.getId(),
                                null, null,
                                KeyParseUtil.getResourceType(key),
                                KeyParseUtil.getResourceName(key));
                saveOrUpdateResource(devopsEnvResourceDTO,
                        oldDevopsEnvResourceDTO,
                        devopsEnvResourceDetailDTO,
                        appServiceInstanceDTO);
            }
            devopsServiceDTO.setStatus(ServiceStatus.RUNNING.getStatus());
            devopsServiceService.baseUpdate(devopsServiceDTO);
            DevopsEnvCommandDTO newDevopsEnvCommandDTO = devopsEnvCommandService
                    .baseQueryByObject(ObjectType.SERVICE.getType(), devopsServiceDTO.getId());
            newDevopsEnvCommandDTO.setStatus(CommandStatus.SUCCESS.getStatus());
            devopsEnvCommandService.baseUpdate(newDevopsEnvCommandDTO);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void namespaceInfo(String msg, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        devopsClusterDTO.setNamespaces(msg);
        devopsClusterService.baseUpdate(devopsClusterDTO);

    }

    @Override
    public void upgradeCluster(String key, String msg) {
        //0.10.0-0.11.0  初始化集群信息
        logger.info("upgradeCluster message: {}", msg);
        UpgradeClusterVO upgradeClusterVO = json.deserialize(msg, UpgradeClusterVO.class);
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQueryByToken(upgradeClusterVO.getToken());
        if (devopsClusterDTO == null) {
            logger.info("the cluster is not exist: {}", upgradeClusterVO.getToken());
            return;
        }
        if (devopsClusterDTO.getInit() != null) {
            logger.info("the cluster has bean init: {}", devopsClusterDTO.getName());
            return;
        }
        if (upgradeClusterVO.getEnvs() != null) {
            upgradeClusterVO.getEnvs().forEach(clusterEnv -> {
                DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(clusterEnv.getEnvId());
                if (devopsEnvironmentDTO != null && devopsEnvironmentDTO.getCode().equals(clusterEnv.getNamespace())) {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
                    if (projectDTO.getOrganizationId().equals(devopsClusterDTO.getOrganizationId())) {
                        DevopsClusterProPermissionDTO devopsClusterProPermissionDTO = new DevopsClusterProPermissionDTO();
                        devopsClusterProPermissionDTO.setProjectId(projectDTO.getId());
                        devopsClusterProPermissionDTO.setClusterId(devopsClusterDTO.getId());
                        devopsClusterProPermissionService.baseInsertPermission(devopsClusterProPermissionDTO);
                        devopsEnvironmentDTO.setClusterId(devopsClusterDTO.getId());
                        devopsEnvironmentService.baseUpdate(devopsEnvironmentDTO);
                    }
                }
            });
            devopsClusterDTO.setSkipCheckProjectPermission(false);
        }
        devopsClusterDTO.setInit(true);
        devopsClusterService.baseUpdate(devopsClusterDTO);
        GitConfigVO gitConfigVO = gitUtil.getGitConfig(devopsClusterDTO.getId());
    }


    @Override
    @Saga(code = SagaTopicCodeConstants.TEST_POD_UPDATE_SAGA,
            description = "测试应用Pod升级(test pod update saga)", inputSchema = "{}")
    public void testPodUpdate(String key, String msg, Long clusterId) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);
        String status = K8sUtil.changePodStatus(v1Pod);
        if (status.equals("Running")) {
            PodUpdateVO podUpdateVO = new PodUpdateVO();
            Optional<V1Container> container = v1Pod.getSpec().getContainers().stream().filter(v1Container -> v1Container.getName().contains("automation-test")).findFirst();
            if (container.isPresent()) {
                podUpdateVO.setConName(container.get().getName());
            }
            podUpdateVO.setPodName(v1Pod.getMetadata().getName());
            podUpdateVO.setReleaseNames(KeyParseUtil.getReleaseName(key));
            podUpdateVO.setStatus(0L);

            producer.applyAndReturn(
                    StartSagaBuilder
                            .newBuilder()
                            .withLevel(ResourceLevel.SITE)
                            .withRefType("")
                            .withSagaCode(SagaTopicCodeConstants.TEST_POD_UPDATE_SAGA),
                    builder -> builder
                            .withPayloadAndSerialize(podUpdateVO)
                            .withRefId(""));
        }
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.TEST_JOB_LOG_SAGA,
            description = "测试Job日志(test job log saga)", inputSchema = "{}")
    public void testJobLog(String key, String msg, Long clusterId) {
        JobLogVO jobLogVO = json.deserialize(msg, JobLogVO.class);
        PodUpdateVO podUpdateVO = new PodUpdateVO();
        podUpdateVO.setReleaseNames(KeyParseUtil.getReleaseName(key));
        if (jobLogVO.getSucceed() != null && jobLogVO.getSucceed()) {
            podUpdateVO.setStatus(1L);
        } else {
            podUpdateVO.setStatus(-1L);
        }
        podUpdateVO.setLogFile(jobLogVO.getLog());
        String input = gson.toJson(podUpdateVO);
        logger.info(input);

        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.SITE)
                        .withRefType("")
                        .withSagaCode(SagaTopicCodeConstants.TEST_JOB_LOG_SAGA),
                builder -> builder
                        .withPayloadAndSerialize(podUpdateVO)
                        .withRefId(""));
    }

    @Override
    @Saga(code = SagaTopicCodeConstants.TEST_STATUS_SAGA,
            description = "测试Release状态(test status saga)", inputSchema = "{}")
    public void getTestAppStatus(String key, String msg, Long clusterId) {
        logger.info(msg);
        List<TestReleaseStatusPayload> testReleaseStatusPayloads = JSONArray.parseArray(msg, TestReleaseStatusPayload.class);
        List<PodUpdateVO> podUpdateVOS = new ArrayList<>();
        for (TestReleaseStatusPayload testReleaseStatu : testReleaseStatusPayloads) {
            PodUpdateVO podUpdateVO = new PodUpdateVO();
            podUpdateVO.setReleaseNames(testReleaseStatu.getReleaseName());
            if (testReleaseStatu.getStatus().equals("running")) {
                podUpdateVO.setStatus(1L);
            } else {
                podUpdateVO.setStatus(0L);
            }
            podUpdateVOS.add(podUpdateVO);
        }
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.SITE)
                        .withRefType("")
                        .withSagaCode(SagaTopicCodeConstants.TEST_STATUS_SAGA),
                builder -> builder
                        .withPayloadAndSerialize(podUpdateVOS)
                        .withRefId(""));
    }

    @Override
    public void getCertManagerInfo(String payloadMsg, Long clusterId) {
        if (payloadMsg == null) {
            agentCommandService.createCertManager(clusterId);
        }
    }


    private void updateResourceStatus(Long envId, DevopsEnvCommandDTO devopsEnvCommandDTO, InstanceStatus running, ServiceStatus running2, IngressStatus running3, CertificationStatus active) {
        if (devopsEnvCommandDTO.getObject().equals(INSTANCE_KIND)) {
            AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsEnvCommandDTO.getObjectId());
            appServiceInstanceDTO.setStatus(running.getStatus());
            appServiceInstanceService.baseUpdate(appServiceInstanceDTO);
        }
        if (devopsEnvCommandDTO.getObject().equals(SERVICE_KIND)) {
            DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQuery(devopsEnvCommandDTO.getObjectId());
            devopsServiceDTO.setStatus(running2.getStatus());
            devopsServiceService.baseUpdate(devopsServiceDTO);
        }
        if (devopsEnvCommandDTO.getObject().equals(INGRESS_KIND)) {
            DevopsIngressDTO devopsIngressDTO = devopsIngressService.baseQuery(devopsEnvCommandDTO.getObjectId());
            devopsIngressService.baseUpdateStatus(envId, devopsIngressDTO.getName(), running3.getStatus());
        }
        if (devopsEnvCommandDTO.getObject().equals(CERTIFICATE_KIND)) {
            CertificationDTO certificationDTO = certificationService.baseQueryById(devopsEnvCommandDTO.getObjectId());
            certificationDTO.setStatus(active.getStatus());
            certificationService.baseUpdateStatus(certificationDTO);
        }
        //todo 其他资源的状态同步也要加上
    }

    @Override
    public void certIssued(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        try {
            String certName = KeyParseUtil.getValue(key, "Cert");
            CertificationDTO certificationDTO = certificationService.baseQueryByEnvAndName(envId, certName);
            if (certificationDTO != null) {
                DevopsEnvCommandDTO commandDTO = devopsEnvCommandService.baseQuery(certificationDTO.getCommandId());
                Object obj = objectMapper.readValue(msg, Object.class);
                String crt = ((LinkedHashMap) ((LinkedHashMap) obj).get("data")).get("tls.crt").toString();
                X509Certificate certificate = CertificateUtil.decodeCert(Base64Util.base64Decoder(crt));
                Date validFrom = certificate.getNotBefore();
                Date validUntil = certificate.getNotAfter();
                if (!(validFrom.equals(certificationDTO.getValidFrom())
                        && validUntil.equals(certificationDTO.getValidUntil()))) {
                    certificationDTO.setValid(validFrom, validUntil);
                    certificationService.baseUpdateValidField(certificationDTO);
                }
                boolean commandNotExist = commandDTO == null;
                if (commandNotExist) {
                    commandDTO = new DevopsEnvCommandDTO();
                }
                commandDTO.setObject(ObjectType.CERTIFICATE.getType());
                commandDTO.setCommandType(CommandType.CREATE.getType());
                commandDTO.setObjectId(certificationDTO.getId());
                commandDTO.setStatus(CommandStatus.SUCCESS.getStatus());
                commandDTO.setSha(KeyParseUtil.getValue(key, "commit"));
                if (commandNotExist) {
                    commandDTO = devopsEnvCommandService.baseCreate(commandDTO);
                } else {
                    devopsEnvCommandService.baseUpdate(commandDTO);
                }
                certificationDTO.setCommandId(commandDTO.getId());
                certificationService.baseUpdateCommandId(certificationDTO);
            }
        } catch (IOException e) {
            logger.info(e.toString(), e);
        } catch (CertificateException e) {
            logger.info(e.getMessage(), e);
        }
    }

    @Override
    public void certFailed(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        String commitSha = KeyParseUtil.getValue(key, "commit");
        String certName = KeyParseUtil.getValue(key, "Cert");
        CertificationDTO certificationDTO = certificationService.baseQueryByEnvAndName(envId, certName);
        if (certificationDTO != null) {
            DevopsEnvCommandDTO commandDTO = devopsEnvCommandService.baseQuery(certificationDTO.getCommandId());
            String createType = CommandType.CREATE.getType();
            String commandFailedStatus = CommandStatus.FAILED.getStatus();
            boolean commandNotExist = commandDTO == null;
            if (commandNotExist) {
                commandDTO = new DevopsEnvCommandDTO();
            }
            if (!createType.equals(commandDTO.getCommandType())
                    || !commandFailedStatus.equals(commandDTO.getStatus())
                    || (!msg.isEmpty() && !msg.equals(commandDTO.getError()))) {
                commandDTO.setObject(ObjectType.CERTIFICATE.getType());
                commandDTO.setCommandType(createType);
                commandDTO.setObjectId(certificationDTO.getId());
                commandDTO.setStatus(commandFailedStatus);
                commandDTO.setSha(commitSha);
                commandDTO.setError(msg);
                if (commandNotExist) {
                    commandDTO = devopsEnvCommandService.baseCreate(commandDTO);
                } else {
                    devopsEnvCommandService.baseUpdate(commandDTO);
                }
                certificationDTO.setCommandId(commandDTO.getId());
                certificationService.baseUpdateCommandId(certificationDTO);
            }
            certificationService.baseClearValidField(certificationDTO.getId());
            String failedStatus = CertificationStatus.FAILED.getStatus();
            if (failedStatus.equals(certificationDTO.getStatus())) {
                certificationDTO.setStatus(failedStatus);
                certificationService.baseUpdateStatus(certificationDTO);
            }
        }

    }


    private Long getEnvId(String key, Long clusterId) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryByClusterIdAndCode(clusterId, KeyParseUtil.getNamespace(key));
        Long envId = null;
        if (devopsEnvironmentDTO != null) {
            envId = devopsEnvironmentDTO.getId();
        }
        return envId;
    }

    @Override
    public void handleNodeSync(String msg, Long clusterId) {
        clusterNodeInfoService.setValueForKey(clusterNodeInfoService.getRedisClusterKey(clusterId), JSONArray.parseArray(msg, AgentNodeInfoVO.class));
    }


    @Override
    public void handlePodMetricsSync(String key, String result, Long clusterId) {
        List<PodMetricsRedisInfoVO> podMetricsRedisInfoVOS = new ArrayList<>();
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        String namespace = KeyParseUtil.getNamespace(key);
        PodMetricsInfoVO podMetricsInfoVO = json.deserialize(result, PodMetricsInfoVO.class);
        if (podMetricsInfoVO.getItems() != null && !podMetricsInfoVO.getItems().isEmpty()) {
            podMetricsInfoVO.getItems().forEach(podMetricsItemVO -> {
                PodMetricsRedisInfoVO podMetricsRedisInfoVO = new PodMetricsRedisInfoVO();
                podMetricsRedisInfoVO.setNamespace(namespace);
                podMetricsRedisInfoVO.setName(podMetricsItemVO.getName());
                podMetricsRedisInfoVO.setClusterCode(devopsClusterDTO.getCode());
                podMetricsRedisInfoVO.setCpu("0");
                podMetricsRedisInfoVO.setMemory("0");
                double[] cpu = {0L};
                double[] memory = {0L};
                podMetricsItemVO.getContainers().forEach(podMetricsContainerVO -> {
                    if (!podMetricsContainerVO.getUsage().getCpu().equals("0")) {
                        cpu[0] = cpu[0] + TypeUtil.objToLong(podMetricsContainerVO.getUsage().getCpu().substring(0, (podMetricsContainerVO.getUsage().getCpu().length() - 1)));
                    }
                    if (!podMetricsContainerVO.getUsage().getMemory().equals("0")) {
                        memory[0] = memory[0] + TypeUtil.objToLong(podMetricsContainerVO.getUsage().getMemory().substring(0, (podMetricsContainerVO.getUsage().getMemory().length() - 2)));
                    }
                });
                if (cpu[0] != 0L) {
                    podMetricsRedisInfoVO.setCpu(TypeUtil.objToLong(new Double(Math.ceil(cpu[0] / (1000*1000))).longValue()) + "m");
                }
                if (memory[0] != 0L) {
                    podMetricsRedisInfoVO.setMemory(new Double(Math.floor(memory[0] / 1024)).longValue() + "Mi");
                }
                podMetricsRedisInfoVOS.add(podMetricsRedisInfoVO);
            });
            agentPodService.handleRealTimePodData(podMetricsRedisInfoVOS);
        }
    }


    @Override
    public void handleConfigUpdate(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        V1ConfigMap v1ConfigMap = json.deserialize(msg, V1ConfigMap.class);
        DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService.baseQueryByEnvIdAndName(envId, v1ConfigMap.getMetadata().getName());
        DevopsConfigMapVO devopsConfigMapVO = new DevopsConfigMapVO();
        devopsConfigMapVO.setDescription(v1ConfigMap.getMetadata().getName() + " config");
        devopsConfigMapVO.setEnvId(envId);
        devopsConfigMapVO.setName(v1ConfigMap.getMetadata().getName());
        devopsConfigMapVO.setValue(v1ConfigMap.getData());
        if (devopsConfigMapDTO == null) {
            devopsConfigMapVO.setType(CREATE_TYPE);
            devopsConfigMapService.createOrUpdate(devopsEnvironmentDTO.getProjectId(), true, devopsConfigMapVO);
        } else {
            devopsConfigMapVO.setId(devopsConfigMapDTO.getId());
            devopsConfigMapVO.setType(UPDATE_TYPE);
            if (devopsConfigMapVO.getValue().equals(gson.fromJson(devopsConfigMapDTO.getValue(), Map.class))) {
                return;
            } else {
                devopsConfigMapService.createOrUpdate(devopsEnvironmentDTO.getProjectId(), true, devopsConfigMapVO);
            }
        }

    }

    @Override
    public void operateDockerRegistrySecretResp(String key, String result, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        DevopsRegistrySecretDTO devopsRegistrySecretDTO = devopsRegistrySecretService.baseQueryByEnvAndName(envId, KeyParseUtil.getResourceName(key));
        if (result.equals("failed")) {
            devopsRegistrySecretDTO.setStatus(false);
        } else {
            devopsRegistrySecretDTO.setStatus(true);
        }
        devopsRegistrySecretService.baseUpdate(devopsRegistrySecretDTO);
    }
}

