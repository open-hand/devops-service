package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_RESOURCE_INSERT;
import static io.choerodon.devops.infra.constant.GitOpsConstants.DATE_PATTERN;
import static io.choerodon.devops.infra.constant.GitOpsConstants.THREE_MINUTE_MILLISECONDS;
import static io.choerodon.devops.infra.constant.MiscConstants.CREATE_TYPE;
import static io.choerodon.devops.infra.constant.MiscConstants.UPDATE_TYPE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.*;
import io.choerodon.devops.app.eventhandler.constants.CertManagerConstants;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.eventhandler.payload.OperationPodPayload;
import io.choerodon.devops.app.eventhandler.payload.TestReleaseStatusPayload;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.*;

/**
 * Created by Zenger on 2018/4/17.
 */
@Service
public class AgentMsgHandlerServiceImpl implements AgentMsgHandlerService {

    public static final String CHOERODON_IO_REPLICAS_STRATEGY = "choerodon.io/replicas-strategy";
    public static final String EVICTED = "Evicted";
    private static final String CHOERODON_IO_PARENT_WORKLOAD_PARENT_NAME = "choerodon.io/parent-workload-name";
    private static final String CHOERODON_IO_PARENT_WORKLOAD_PARENT = "choerodon.io/parent-workload";
    private static final String CHOERODON_IO_NETWORK_SERVICE_INSTANCES = "choerodon.io/network-service-instances";
    private static final String CHOERODON_IO_V1_COMMAND = "choerodon.io/v1-command";
    private static final String PENDING = "Pending";
    private static final String METADATA = "metadata";
    private static final String SERVICE_KIND = "service";
    private static final String INGRESS_KIND = "ingress";
    private static final String CONFIGMAP_KIND = "configmap";
    private static final String C7NHELMRELEASE_KIND = "c7nhelmrelease";
    private static final String CERTIFICATE_KIND = "certificate";
    private static final String SECRET_KIND = "secret";
    private static final String PERSISTENT_VOLUME_KIND = "persistentvolume";
    private static final String PERSISTENT_VOLUME_CLAIM_KIND = "persistentvolumeclaim";
    private static final String DEPLOYMENT = "deployment";
    private static final String JOB = "job";
    private static final String DAEMONSET = "daemonset";
    private static final String CRON_JOB = "cronjob";
    private static final String STATEFULSET = "statefulset";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentMsgHandlerServiceImpl.class);
    private static final String RESOURCE_VERSION = "resourceVersion";
    private static final String ENV_NOT_EXIST = "env not exists: {}";
    private static final String INIT_JOB_NAME_SUFFIX = "-init-db";
    private static final Integer MAX_LOG_MSG_LENGTH = 60000;
    private static JSON json = new JSON();
    private static ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    DevopsCommandEventService devopsCommandEventService;
    private final Gson gson = new Gson();
    @Autowired
    private DevopsEnvPodService devopsEnvPodService;
    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;
    @Autowired
    private DevopsEnvResourceDetailService devopsEnvResourceDetailService;
    @Autowired
    private DevopsServiceService devopsServiceService;
    @Autowired
    private DevopsEnvCommandLogService devopsEnvCommandLogService;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private AppServiceService appServiceService;
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
    private AppServiceMapper appServiceMapper;
    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;
    @Autowired
    private DevopsPvcService devopsPvcService;
    @Autowired
    private DevopsPvService devopsPvService;
    @Autowired
    private DevopsPvcMapper devopsPvcMapper;
    @Autowired
    private DevopsPvMapper devopsPvMapper;
    @Autowired
    private DevopsCertManagerRecordMapper devopsCertManagerRecordMapper;
    @Autowired
    private DevopsCertManagerMapper devopsCertManagerMapper;
    @Autowired
    @Lazy
    private SendNotificationService sendNotificationService;
    @Autowired
    private DevopsSecretMapper devopsSecretMapper;
    @Autowired
    private WorkloadService workloadService;
    @Autowired
    private DevopsDeploymentService devopsDeploymentService;
    @Autowired
    private DevopsStatefulSetService devopsStatefulSetService;
    @Autowired
    private DevopsJobService devopsJobService;
    @Autowired
    private DevopsDaemonSetService devopsDaemonSetService;
    @Autowired
    private DevopsCronJobService devopsCronJobService;

    @Autowired
    private ChartResourceOperator chartResourceOperator;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;
    //    @Autowired
//    private DevopsHzeroDeployDetailsService devopsHzeroDeployDetailsService;
    @Autowired
    private AppExceptionRecordService appExceptionRecordService;


    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_POD_READY,
            description = "pod状态更新",
            inputSchemaClass = PodReadyEventVO.class)
    public void handlerUpdatePodMessage(String key, String msg, Long envId) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);

        String releaseName = KeyParseUtil.getReleaseName(key);
        AppServiceInstanceDTO appServiceInstanceDTO = null;

        Map<String, String> labels = v1Pod.getMetadata().getLabels();
        // pod 没有完整的workload标签
        boolean isWorkloadLabelEmpty = StringUtils.isEmpty(labels.get(CHOERODON_IO_PARENT_WORKLOAD_PARENT_NAME)) || StringUtils.isEmpty(labels.get(CHOERODON_IO_PARENT_WORKLOAD_PARENT));
        List<V1OwnerReference> v1OwnerReferences = v1Pod.getMetadata().getOwnerReferences();
        // pod 没有属主信息，比如直接创建的pod
        boolean isReferencesEmpty = (v1OwnerReferences == null || v1OwnerReferences.isEmpty());
        // 没有属主信息并且没有workload标签，舍弃
        if (isReferencesEmpty && isWorkloadLabelEmpty) {
            return;
        }
        if (!StringUtils.isEmpty(releaseName)) {
            appServiceInstanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
            if (appServiceInstanceDTO == null && isWorkloadLabelEmpty) {
                LOGGER.info("instance not found");
                return;
            }
        }
        String parentName = labels.get(CHOERODON_IO_PARENT_WORKLOAD_PARENT_NAME);
        String parentType = labels.get(CHOERODON_IO_PARENT_WORKLOAD_PARENT);
        Long instanceId;
        if (appServiceInstanceDTO == null) {
            instanceId = workloadService.getWorkloadId(envId, parentName, parentType);
        } else {
            instanceId = appServiceInstanceDTO.getId();
        }
        DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
        DevopsEnvResourceDTO newDevopsEnvResourceDTO =
                devopsEnvResourceService.baseQueryOptions(
                        instanceId,
                        null,
                        null,
                        KeyParseUtil.getResourceType(key),
                        v1Pod.getMetadata().getName());
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO();
        devopsEnvResourceDetailDTO.setMessage(msg);
        devopsEnvResourceDTO.setInstanceId(instanceId);
        devopsEnvResourceDTO.setKind(KeyParseUtil.getResourceType(key));
        devopsEnvResourceDTO.setEnvId(envId);
        devopsEnvResourceDTO.setName(v1Pod.getMetadata().getName());
        devopsEnvResourceDTO.setReversion(TypeUtil.objToLong(v1Pod.getMetadata().getResourceVersion()));
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
        devopsEnvPodDTO.setOwnerRefKind(labels.get(CHOERODON_IO_PARENT_WORKLOAD_PARENT));
        devopsEnvPodDTO.setOwnerRefName(labels.get(CHOERODON_IO_PARENT_WORKLOAD_PARENT_NAME));
        devopsEnvPodDTO.setEnvId(envId);

        Boolean flag = false;
        if (appServiceInstanceDTO != null && appServiceInstanceDTO.getId() != null) {
            List<DevopsEnvPodDTO> devopsEnvPodEList = devopsEnvPodService
                    .baseListByInstanceId(appServiceInstanceDTO.getId());
            handleEnvPod(v1Pod, appServiceInstanceDTO, resourceVersion, devopsEnvPodDTO, flag, devopsEnvPodEList);
//            // 实例下的pod状态变为ready,发送通知
//
//            if (Boolean.TRUE.equals(devopsEnvPodDTO.getReady())) {
//                DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, appServiceInstanceDTO.getId());
//                if (devopsDeployAppCenterEnvDTO != null) {
//                    DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = devopsHzeroDeployDetailsService.baseQueryByAppId(devopsDeployAppCenterEnvDTO.getId());
//                    if (devopsHzeroDeployDetailsDTO != null) {
//                        PodReadyEventVO podReadyEventVO = new PodReadyEventVO(TypeUtil.objToLong(labels.get(CHOERODON_IO_V1_COMMAND)), devopsHzeroDeployDetailsDTO);
//                        producer.applyAndReturn(
//                                StartSagaBuilder
//                                        .newBuilder()
//                                        .withLevel(ResourceLevel.SITE)
//                                        .withRefType("")
//                                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_POD_READY),
//                                builder -> builder
//                                        .withPayloadAndSerialize(podReadyEventVO)
//                                        .withRefId(""));
//                    }
//                }
//
//
//            }
        } else {
            DevopsEnvPodDTO devopsEnvPodDTORecord = devopsEnvPodService.baseQueryByEnvIdAndName(envId, v1Pod.getMetadata().getName());
            if (devopsEnvPodDTORecord != null) {
                if ((v1Pod.getStatus().getPhase() != null && v1Pod.getStatus().getPhase().equals(EVICTED)) || (v1Pod.getStatus().getReason() != null && v1Pod.getStatus().getReason().equals(EVICTED))) {
                    devopsEnvPodService.baseDeleteById(devopsEnvPodDTORecord.getId());
                    devopsEnvResourceService.deleteByEnvIdAndKindAndName(envId, ResourceType.POD.getType(), devopsEnvPodDTORecord.getName());
                } else if (!resourceVersion.equals(devopsEnvPodDTORecord.getResourceVersion())) {
                    devopsEnvPodDTORecord.setStatus(status);
                    devopsEnvPodDTORecord.setResourceVersion(resourceVersion);
                    devopsEnvPodDTORecord.setReady(getReadyValue(status, v1Pod));
                    devopsEnvPodDTORecord.setRestartCount(K8sUtil.getRestartCountForPod(v1Pod));
                    devopsEnvPodDTORecord.setNodeName(v1Pod.getSpec().getNodeName());
                    devopsEnvPodService.baseUpdate(devopsEnvPodDTORecord);
                }

            } else {
                devopsEnvPodService.baseCreate(devopsEnvPodDTO);
            }

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
        if (ObjectUtils.isEmpty(v1Pod.getStatus()) || ObjectUtils.isEmpty(v1Pod.getStatus().getContainerStatuses())) {
            return false;
        }
        return !PENDING.equals(podStatus) && v1Pod.getStatus().getContainerStatuses().stream().map(V1ContainerStatus::getReady).reduce((one, another) -> mapNullToFalse(one) && mapNullToFalse(another)).orElse(Boolean.FALSE);
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
        //如果pod的状态是被驱逐的，则应该直接删掉
        if ((v1Pod.getStatus().getPhase() != null && v1Pod.getStatus().getPhase().equals(EVICTED)) || (v1Pod.getStatus().getReason() != null && v1Pod.getStatus().getReason().equals(EVICTED))) {
            devopsEnvPodService.baseDeleteByNameAndEnvId(v1Pod.getMetadata().getName(), appServiceInstanceDTO.getEnvId());
            devopsEnvResourceService.deleteByKindAndNameAndInstanceId(ResourceType.POD.getType(), v1Pod.getMetadata().getName(), appServiceInstanceDTO.getId());
        } else {
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
                            devopsEnvPodDTO.setNodeName(v1Pod.getSpec().getNodeName());
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
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmInstallResourceInfo(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        ReleasePayloadVO releasePayloadVO = JSONArray.parseObject(msg, ReleasePayloadVO.class);
        List<Resource> resources = JSONArray.parseArray(releasePayloadVO.getResources(), Resource.class);
        if (LOGGER.isInfoEnabled()) {
            if (resources == null) {
                LOGGER.info("Install resource: resources null...");
            } else {
                LOGGER.info("Install resource: resource size: {}", resources.size());
                resources.forEach(resource -> LOGGER.info("Install resource: resource kind {} resource name {}", resource.getKind(), resource.getName()));
            }
        }
        String releaseName = releasePayloadVO.getName();
        AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
        if (appServiceInstanceDTO != null) {
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                    .baseQuery(appServiceInstanceDTO.getCommandId());
            if (devopsEnvCommandDTO != null) {
                devopsEnvCommandDTO.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);

                if (StringUtils.isEmpty(releasePayloadVO.getCommit())) {
                    LOGGER.warn("Unexpected empty value '{}' for commit of release payload.", releasePayloadVO.getCommit());
                } else {
                    Long effectCommandId = getEffectCommandId(appServiceInstanceDTO.getId(), releasePayloadVO.getCommit());
                    if (effectCommandId != null) {
                        appServiceInstanceDTO.setEffectCommandId(effectCommandId);
                        LOGGER.info("Found command by sha. command id: {}", effectCommandId);
                    } else {
                        LOGGER.info("Command with object id {} and sha {} is not found", appServiceInstanceDTO.getId(), releasePayloadVO.getCommit());
                    }
                }

                // 如果通过sha查不到
                if (appServiceInstanceDTO.getEffectCommandId() == null) {
                    if (releasePayloadVO.getCommand() == null) {
                        LOGGER.warn("Unexpected empty value '{}' for command of release payload.", releasePayloadVO.getCommand());
                    } else {
                        LOGGER.info("Getting command from payload. command: {}", releasePayloadVO.getCommand());
                        DevopsEnvCommandDTO effectCommand = devopsEnvCommandService.baseQuery(releasePayloadVO.getCommand());
                        if (effectCommand != null && Objects.equals(effectCommand.getObjectId(), appServiceInstanceDTO.getId())) {
                            appServiceInstanceDTO.setEffectCommandId(releasePayloadVO.getCommand());
                            LOGGER.info("Set the effect command from agent. The instance id is {} and the command id is {}", appServiceInstanceDTO.getId(), releasePayloadVO.getCommand());
                        } else {
                            LOGGER.info("The effect command from agent is invalid for instance {}. It is {}", appServiceInstanceDTO.getId(), releasePayloadVO.getCommand());
                        }
                    }
                }

                setAppVersionIdForInstance(appServiceInstanceDTO, releasePayloadVO, appServiceInstanceDTO.getEffectCommandId());

                appServiceInstanceDTO.setStatus(InstanceStatus.RUNNING.getStatus());
                appServiceInstanceService.baseUpdate(appServiceInstanceDTO);
                installResource(resources, appServiceInstanceDTO);
            }
        }
    }

    private void setAppVersionIdForInstance(AppServiceInstanceDTO appServiceInstanceDTO, ReleasePayloadVO releasePayloadVO, Long effectCommandId) {
        // 兼容集群组件
        if (appServiceInstanceDTO.getAppServiceId() != null) {
            // 市场实例通过effect Command查，因为同一个市场服务的多个发布对象之间，
            // chartVersion不一定发生了变化，所以需要这个来确定具体是部署哪个发布对象，
            // TODO 普通实例也应该可以通过生效的command来查版本id
            if (AppSourceType.MARKET.getValue().equals(appServiceInstanceDTO.getSource()) || AppSourceType.MIDDLEWARE.getValue().equals(appServiceInstanceDTO.getSource())) {
                if (effectCommandId != null) {
                    DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(effectCommandId);
                    appServiceInstanceDTO.setAppServiceVersionId(devopsEnvCommandDTO.getObjectVersionId());
                }
            } else {
                AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQueryByAppServiceIdAndVersion(appServiceInstanceDTO.getAppServiceId(), releasePayloadVO.getChartVersion());
                appServiceInstanceDTO.setAppServiceVersionId(Objects.requireNonNull(appServiceVersionDTO.getId()));
            }
        } else {
            appServiceInstanceDTO.setComponentVersion(Objects.requireNonNull(releasePayloadVO.getChartVersion()));
        }
    }

    private Long getEffectCommandId(Long instanceId, String releaseCommit) {
        try {
            DevopsEnvCommandDTO result = devopsEnvCommandService.queryByInstanceIdAndCommitSha(instanceId, releaseCommit);
            return result == null ? null : result.getId();
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed to query effect command. instanceId: {}, releaseCommit: {}", instanceId, releaseCommit);
                LOGGER.debug("The ex is:", e);
            } else if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Failed to query effect command. instanceId: {}, releaseCommit: {}, the exception class is {}", instanceId, releaseCommit, e.getClass());
            }
            return null;
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmInstallJobInfo(String key, String msg, Long clusterId) {
        if ("null".equals(msg)) {
            return;
        }
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
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
            // 这里要设置Command的状态 因为811行 GitOps sync方法里改了command的状态
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                    .baseQuery(appServiceInstanceDTO.getCommandId());
            devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
            devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
        } catch (Exception e) {
            throw new CommonException(DEVOPS_RESOURCE_INSERT, e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resourceUpdate(String key, String msg, Long clusterId) {
        try {
            LOGGER.debug("key:{} msg:{} clusterId:{}", key, msg, clusterId);
            Long envId = getEnvId(key, clusterId);

            String type = KeyParseUtil.getResourceType(key);

            if (envId == null && !ResourceType.PERSISTENT_VOLUME.getType().equals(type)) {
                LOGGER.info("{} {} clusterId:{}", ENV_NOT_EXIST, KeyParseUtil.getNamespace(key), clusterId);
                LOGGER.info("resource name: {}", KeyParseUtil.getResourceName(key));
                return;
            }

            Object obj = objectMapper.readValue(msg, Object.class);
            DevopsEnvResourceDTO devopsEnvResourceDTO = new DevopsEnvResourceDTO();
            DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO = new DevopsEnvResourceDetailDTO();
            devopsEnvResourceDetailDTO.setMessage(msg);
            devopsEnvResourceDTO.setKind(type);
            devopsEnvResourceDTO.setEnvId(envId);
            devopsEnvResourceDTO.setName(KeyParseUtil.getResourceName(key));
            devopsEnvResourceDTO.setReversion(
                    TypeUtil.objToLong(
                            ((LinkedHashMap) ((LinkedHashMap) obj).get(METADATA)).get(RESOURCE_VERSION).toString()));
            String releaseName = KeyParseUtil.getReleaseName(key);
            DevopsEnvResourceDTO oldDevopsEnvResourceDTO;
            AppServiceInstanceDTO appServiceInstanceDTO = null;

            ResourceType resourceType = ResourceType.forString(KeyParseUtil.getResourceType(key));
            if (resourceType == null) {
                resourceType = ResourceType.MISSTYPE;
            }
            if (releaseName != null) {
                appServiceInstanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
            }

            // 保存chart内资源信息到对应资源表
            ChartResourceOperatorService chartResourceOperatorService = chartResourceOperator.getOperatorMap().get(resourceType.getType());
            // 如果resourceType是job，且名称以-init-db结尾，那么认为是init-job的资源，不保存到工作负载中
            if (chartResourceOperatorService != null && appServiceInstanceDTO != null && !(ResourceType.JOB.equals(resourceType) && devopsEnvResourceDTO.getName().endsWith(INIT_JOB_NAME_SUFFIX))) {
                chartResourceOperatorService.saveOrUpdateChartResource(msg, appServiceInstanceDTO);
            }

            switch (resourceType) {
                case INGRESS:
                    oldDevopsEnvResourceDTO =
                            devopsEnvResourceService.baseQueryOptions(
                                    appServiceInstanceDTO == null ? null : appServiceInstanceDTO.getId(),
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
                            devopsEnvResourceDetailDTO, appServiceInstanceDTO);
                    break;
                case POD:
                    handlerUpdatePodMessage(key, msg, envId);
                    break;
                case SERVICE:
                    handleUpdateServiceMsg(key, envId, msg, devopsEnvResourceDTO);
                    break;
                case CONFIGMAP:
                case SECRET:
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
                case PERSISTENT_VOLUME_CLAIM:
                    handleUpdatePvcMsg(key, envId, msg, devopsEnvResourceDTO, devopsEnvResourceDetailDTO);
                    break;
                case PERSISTENT_VOLUME:
                    // env id是null的
                    handleUpdatePvMsg(key, clusterId, msg, devopsEnvResourceDTO, devopsEnvResourceDetailDTO);
                    break;
                case DEPLOYMENT:
                    handleUpdateWorkloadMsg(key, envId, msg, null, devopsEnvResourceDTO, devopsEnvResourceDetailDTO, appServiceInstanceDTO);
                    DevopsDeploymentDTO deploymentDTO = devopsDeploymentService.baseQueryByEnvIdAndName(envId, KeyParseUtil.getResourceName(key));
                    // 部署组创建的deployment，如果副本变为0则更新应用状态为停止
                    if (deploymentDTO != null && WorkloadSourceTypeEnums.DEPLOY_GROUP.getType().equals(deploymentDTO.getSourceType())) {
                        V1Deployment v1Deployment = K8sUtil.deserialize(msg, V1Deployment.class);
                        if (v1Deployment.getSpec().getReplicas() == 0 && !InstanceStatus.STOPPED.getStatus().equals(deploymentDTO.getStatus())) {
                            deploymentDTO.setStatus(InstanceStatus.STOPPED.getStatus());
                            devopsDeploymentService.baseUpdate(deploymentDTO);
                        } else if (v1Deployment.getSpec().getReplicas() > 0 && !InstanceStatus.RUNNING.getStatus().equals(deploymentDTO.getStatus())) {
                            deploymentDTO.setStatus(InstanceStatus.RUNNING.getStatus());
                            devopsDeploymentService.baseUpdate(deploymentDTO);
                        }
                    }
                    if (appServiceInstanceDTO != null) {
                        // 保存应用异常数据（采集监控报表数据）
                        appExceptionRecordService.createOrUpdateExceptionRecord(ResourceType.DEPLOYMENT.getType(), msg, appServiceInstanceDTO);
                    }
                    break;
                case JOB:
                case DAEMONSET:
                case CRON_JOB:
                case STATEFULSET:
                    handleUpdateWorkloadMsg(key, envId, msg, appServiceInstanceDTO == null ? null : appServiceInstanceDTO.getCommandId(), devopsEnvResourceDTO, devopsEnvResourceDetailDTO, appServiceInstanceDTO);
                    if (appServiceInstanceDTO != null) {
                        // 保存应用异常数据（采集监控报表数据）
                        appExceptionRecordService.createOrUpdateExceptionRecord(ResourceType.STATEFULSET.getType(), msg, appServiceInstanceDTO);
                    }
                    break;
                default:
                    // 默认为Release对象
                    if (releaseName != null) {
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
            LOGGER.info("Unexpected exception occurred when processing resourceUpdate. The exception is", e);
        }
    }

    private void handleUpdateWorkloadMsg(String key, Long envId, String msg, Long commandId, DevopsEnvResourceDTO devopsEnvResourceDTO, DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO, AppServiceInstanceDTO appServiceInstanceDTO) {
        DevopsEnvResourceDTO oldDevopsEnvResourceDTO =
                devopsEnvResourceService.baseQueryOptions(
                        appServiceInstanceDTO == null ? null : appServiceInstanceDTO.getId(),
                        appServiceInstanceDTO == null ? null : commandId,
                        envId,
                        KeyParseUtil.getResourceType(key),
                        KeyParseUtil.getResourceName(key));
        saveOrUpdateResource(devopsEnvResourceDTO, oldDevopsEnvResourceDTO,
                devopsEnvResourceDetailDTO, appServiceInstanceDTO);
    }

    /**
     * 处理更新PV的消息
     * 主要工作是存devops_env_resource纪录，devops_env_resource_detail纪录，
     * 更新PV相关纪录的状态
     *
     * @param key                        Agent消息的key
     * @param clusterId                  集群id
     * @param msg                        Agent消息内容
     * @param devopsEnvResourceDTO       已经构建好的纪录
     * @param devopsEnvResourceDetailDTO 已经构建好的纪录
     */
    private void handleUpdatePvMsg(String key, Long clusterId, String msg,
                                   DevopsEnvResourceDTO devopsEnvResourceDTO,
                                   DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO) {
        LOGGER.info("Update pv message.clusterId is {}", clusterId);
        String resourceName = KeyParseUtil.getResourceName(key);
        LOGGER.info("pv name is {}", resourceName);

        DevopsPvDTO devopsPvDTO = devopsPvService.queryWithEnvByClusterIdAndName(clusterId, resourceName);
        if (devopsPvDTO == null) {
            // 这个逻辑意味着自定义资源中的PV的message信息是不存数据库的
            LOGGER.info("PV with clusterId {} and name {} is not found in database", clusterId, resourceName);
            return;
        }

        devopsEnvResourceDTO.setEnvId(devopsPvDTO.getEnvId());

        DevopsEnvResourceDTO oldDevopsEnvResourceDTO =
                devopsEnvResourceService.baseQueryOptions(
                        null,
                        null,
                        devopsPvDTO.getEnvId(),
                        ResourceType.PERSISTENT_VOLUME.getType(),
                        resourceName);
        saveOrUpdateResource(devopsEnvResourceDTO, oldDevopsEnvResourceDTO, devopsEnvResourceDetailDTO, null);

        V1PersistentVolume pv = json.deserialize(msg, V1PersistentVolume.class);
        devopsPvDTO.setStatus(pv.getStatus().getPhase());
        if (devopsPvDTO.getStatus().equals(PvStatus.BOUND.getStatus())) {
            devopsPvDTO.setPvcName(pv.getSpec().getClaimRef().getName());
        }

        CustomContextUtil.executeRunnableInCertainContext(devopsPvDTO.getLastUpdatedBy(), () -> devopsPvMapper.updateByPrimaryKeySelective(devopsPvDTO));
    }

    /**
     * 处理更新PVC的消息
     * 主要工作是存devops_env_resource纪录，devops_env_resource_detail纪录，
     * 更新PVC相关纪录的状态
     *
     * @param key                        Agent消息的key
     * @param envId                      环境id
     * @param msg                        Agent消息内容
     * @param devopsEnvResourceDTO       已经构建好的纪录
     * @param devopsEnvResourceDetailDTO 已经构建好的纪录
     */
    private void handleUpdatePvcMsg(String key, Long envId, String msg,
                                    DevopsEnvResourceDTO devopsEnvResourceDTO,
                                    DevopsEnvResourceDetailDTO devopsEnvResourceDetailDTO) {
        LOGGER.info("Update pvc message.envId is {}", envId);
        String resourceName = KeyParseUtil.getResourceName(key);
        LOGGER.info("pvc name is {}", resourceName);
        DevopsEnvResourceDTO oldDevopsEnvResourceDTO =
                devopsEnvResourceService.baseQueryOptions(
                        null,
                        null,
                        envId,
                        ResourceType.PERSISTENT_VOLUME_CLAIM.getType(),
                        resourceName);
        saveOrUpdateResource(devopsEnvResourceDTO, oldDevopsEnvResourceDTO, devopsEnvResourceDetailDTO, null);
        DevopsPvcDTO devopsPvcDTO = devopsPvcService.queryByEnvIdAndName(envId, resourceName);
        if (devopsPvcDTO == null) {
            LOGGER.info("PVC with envId {} and name {} is not found in database", envId, resourceName);
            return;
        }
        V1PersistentVolumeClaim pv = json.deserialize(msg, V1PersistentVolumeClaim.class);
        devopsPvcDTO.setStatus(pv.getStatus().getPhase());
        CustomContextUtil.executeRunnableInCertainContext(devopsPvcDTO.getLastUpdatedBy(), () -> devopsPvcMapper.updateByPrimaryKeySelective(devopsPvcDTO));
    }

    private void handleUpdateServiceMsg(String key, Long envId, String msg, DevopsEnvResourceDTO devopsEnvResourceDTO) {
        AppServiceInstanceDTO appServiceInstanceDTO;
        V1Service v1Service = json.deserialize(msg, V1Service.class);
        if (v1Service.getMetadata().getAnnotations() != null) {
            DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(v1Service.getMetadata().getName(), envId);
            if (devopsServiceDTO == null) {
                // 如果数据库没有service的对象, 相关的 env_resource 纪录也不需要
                return;
            }
            if (devopsServiceDTO.getType().equals("ClusterIP")) {
                devopsServiceDTO.setClusterIp(v1Service.getSpec().getClusterIP());
                devopsServiceService.baseUpdate(devopsServiceDTO);
            }
            if (devopsServiceDTO.getType().equals("LoadBalancer") &&
                    v1Service.getStatus() != null &&
                    v1Service.getStatus().getLoadBalancer() != null &&
                    !CollectionUtils.isEmpty(v1Service.getStatus().getLoadBalancer().getIngress())) {

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
            portMapVO.setPort(v1ServicePort.getPort());
            portMapVO.setTargetPort(TypeUtil.objToString(v1ServicePort.getTargetPort()));
            portMapVO.setNodePort(v1ServicePort.getNodePort());
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
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        String kind = KeyParseUtil.getResourceType(key);
        String podName = KeyParseUtil.getResourceName(key);
        String podNameSpace = KeyParseUtil.getNamespace(key);
        // 这个表示为init-job，不需要删除
        if (ResourceType.JOB.getType().equals(kind) && podName.endsWith(INIT_JOB_NAME_SUFFIX)) {
            return;
        }
        if (KeyParseUtil.getResourceType(key).equals(ResourceType.POD.getType())) {
            devopsEnvPodService.baseDeleteByName(podName, podNameSpace);
        }

        devopsEnvResourceService.deleteByEnvIdAndKindAndName(
                envId,
                KeyParseUtil.getResourceType(key),
                KeyParseUtil.getResourceName(key));

        ChartResourceOperatorService chartResourceOperatorService = chartResourceOperator.getOperatorMap().get(KeyParseUtil.getResourceType(key));
        if (chartResourceOperatorService != null) {
            chartResourceOperatorService.deleteByEnvIdAndName(envId, KeyParseUtil.getResourceName(key));
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmJobLog(String key, String msg, Long clusterId) {
        byte[] bytes = msg.getBytes();
        byte[] result = new byte[MAX_LOG_MSG_LENGTH];

        if (bytes.length > MAX_LOG_MSG_LENGTH) {
            System.arraycopy(bytes, bytes.length - MAX_LOG_MSG_LENGTH, result, 0, MAX_LOG_MSG_LENGTH);
        } else {
            result = bytes;
        }

        msg = new String(result);


        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateInstanceStatus(String key, String releaseName, Long clusterId, String instanceStatus, String commandStatus, String msg) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        AppServiceInstanceDTO instanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
        if (instanceDTO == null) {
            LOGGER.info("update instance status: the release {} in namespace {} doesn't exist in db", releaseName, KeyParseUtil.getNamespace(key));
            return;
        }

        // 如果实例状态不是 running， 才允许更新
        if (!instanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
            instanceDTO.setStatus(instanceStatus);
            appServiceInstanceService.baseUpdate(instanceDTO);
        }

        // 更新command的状态
        // 先根据commit查询，查不到再根据实例id查询
        String commit = KeyParseUtil.getCommit(key);
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.queryByInstanceIdAndCommitSha(instanceDTO.getId(), commit);
        if (devopsEnvCommandDTO == null) {
            devopsEnvCommandDTO = devopsEnvCommandService.baseQueryByObject(ObjectType.INSTANCE.getType(), instanceDTO.getId());
        }
        devopsEnvCommandDTO.setStatus(commandStatus);
        devopsEnvCommandDTO.setError(msg);
        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);

        // 如果是创建实例失败，发送通知
        if (InstanceStatus.FAILED.getStatus().equals(instanceStatus) && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
            instanceDeployFailed(instanceDTO.getId(), devopsEnvCommandDTO.getId());
            LOGGER.debug("Sending instance notices: env id: {}, instance code {}, createdby: {}", instanceDTO.getEnvId(), instanceDTO.getCode(), instanceDTO.getCreatedBy());
            sendNotificationService.sendInstanceStatusUpdate(instanceDTO, devopsEnvCommandDTO, InstanceStatus.FAILED.getStatus());
        }
        if (!(InstanceStatus.FAILED.getStatus().equals(instanceStatus)) && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
            sendNotificationService.sendInstanceStatusUpdate(instanceDTO, devopsEnvCommandDTO, instanceStatus);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateStartOrStopInstanceStatus(String key, String releaseName, Long clusterId, String instanceStatus, String commandStatus, String payload) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }
        AppServiceInstanceDTO instanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
        if (instanceDTO == null) {
            LOGGER.info("update instance status: the release {} in namespace {} doesn't exist in db", releaseName, KeyParseUtil.getNamespace(key));
            return;
        }

        // 如果实例状态不是 running， 才允许更新
        if (!instanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
            instanceDTO.setStatus(instanceStatus);
            appServiceInstanceService.baseUpdate(instanceDTO);
        }

        // 更新command的状态
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQueryByObject(ObjectType.INSTANCE.getType(), instanceDTO.getId());
        devopsEnvCommandDTO.setStatus(commandStatus);
        devopsEnvCommandDTO.setError(payload);
        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);

        // 更新实例的生效command
        if (CommandStatus.SUCCESS.getStatus().equals(commandStatus)) {
            instanceDTO.setEffectCommandId(devopsEnvCommandDTO.getId());
            instanceDTO.setObjectVersionNumber(instanceDTO.getObjectVersionNumber() + 1);
            appServiceInstanceService.baseUpdate(instanceDTO);
        }

        // 如果是创建实例失败，发送通知
        if (InstanceStatus.FAILED.getStatus().equals(instanceStatus) && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
            instanceDeployFailed(instanceDTO.getId(), devopsEnvCommandDTO.getId());
            LOGGER.debug("Sending instance notices: env id: {}, instance code {}, createdby: {}", instanceDTO.getEnvId(), instanceDTO.getCode(), instanceDTO.getCreatedBy());
            sendNotificationService.sendInstanceStatusUpdate(instanceDTO, devopsEnvCommandDTO, InstanceStatus.FAILED.getStatus());
        }
        if (!(InstanceStatus.FAILED.getStatus().equals(instanceStatus)) && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
            sendNotificationService.sendInstanceStatusUpdate(instanceDTO, devopsEnvCommandDTO, instanceStatus);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handlerDomainCreateMessage(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        V1Ingress ingress = json.deserialize(msg, V1Ingress.class);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmUpgradeJobInfo(String key, String msg, Long clusterId) {
        helmInstallJobInfo(key, msg, clusterId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmUpgradeResourceInfo(String key, String msg, Long clusterId) {
        helmInstallResourceInfo(key, msg, clusterId);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmReleaseDeleteFail(String key, String msg, Long clusterId) {

        updateInstanceStatus(key, KeyParseUtil.getReleaseName(key),
                clusterId,
                InstanceStatus.DELETED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Transactional(rollbackFor = Exception.class)
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
        LOGGER.info("Helm release rollback failed. The key is {}, and the msg is {}", key, msg);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmReleaseInstallFail(String key, String msg, Long clusterId) {
        updateInstanceStatus(
                key, KeyParseUtil.getReleaseName(key),
                clusterId,
                InstanceStatus.FAILED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmReleaseUpgradeFail(String key, String msg, Long clusterId) {

        updateInstanceStatus(key, KeyParseUtil.getReleaseName(key),
                clusterId,
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmReleaseStopFail(String key, String msg, Long clusterId) {
        updateInstanceStatus(key, KeyParseUtil.getReleaseName(key),
                clusterId,
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);

    }


    @Transactional(rollbackFor = Exception.class)
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
                appServiceInstanceService.updateStatus(appServiceInstanceDTO);
            } else if (devopsEnvCommandDTO.getObject().equals(ObjectType.SERVICE.getType())) {
                DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQuery(devopsEnvCommandDTO.getObjectId());
                devopsServiceDTO.setStatus(ServiceStatus.FAILED.getStatus());
                devopsServiceService.updateStatus(devopsServiceDTO);
            } else if (devopsEnvCommandDTO.getObject().equals(ObjectType.INGRESS.getType())) {
                DevopsIngressDTO ingress = devopsIngressService.baseQuery(devopsEnvCommandDTO.getObjectId());
                devopsIngressService.updateStatus(ingress.getEnvId(), ingress.getName(), IngressStatus.FAILED.getStatus());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resourceSync(String key, String msg, Long clusterId) {
        LOGGER.info("Resource sync: key: {}, msg: {}, clusterId: {}", key, msg, clusterId);
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        ResourceSyncPayloadDTO resourceSyncPayloadDTO = JSONArray.parseObject(msg, ResourceSyncPayloadDTO.class);
        ResourceType resourceType = ResourceType.forString(resourceSyncPayloadDTO.getResourceType());
        List<DevopsEnvResourceDTO> devopsEnvResourceDTOS;
        if (resourceType == null) {
            resourceType = ResourceType.MISSTYPE;
        }
        if (resourceSyncPayloadDTO.getResources() == null) {
            LOGGER.info("Resource sync: namespace {} has non resource.", KeyParseUtil.getNamespace(key));
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
                LOGGER.info("Resource sync: miss type: {}", resourceSyncPayloadDTO.getResourceType());
                // TODO 可能需要增加其他资源的同步
                break;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmJobEvent(String key, String msg, Long clusterId) {
        try {
            Long envId = getEnvId(key, clusterId);
            Event event = JSONArray.parseObject(msg, Event.class);
            if (event.getInvolvedObject().getKind().equals(ResourceType.POD.getType())) {
                event.getInvolvedObject().setKind(ResourceType.JOB.getType());
                event.getInvolvedObject().setName(
                        event.getInvolvedObject().getName()
                                .substring(0, event.getInvolvedObject().getName().lastIndexOf('-')));
                insertDevopsCommandEvent(envId, event, ResourceType.JOB.getType(), PodSourceEnums.HELM);
            }
        } catch (Exception e) {
            LOGGER.info("job event:{}", msg);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void helmPodEvent(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        Event event = JSONArray.parseObject(msg, Event.class);
        insertDevopsCommandEvent(envId, event, ResourceType.POD.getType(), PodSourceEnums.HELM);
    }

    @Transactional
    @Override
    public void workloadPodEvent(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        Event event = JSONArray.parseObject(msg, Event.class);
        insertDevopsCommandEvent(envId, event, ResourceType.POD.getType(), PodSourceEnums.WORKLOAD);
    }

    @Override
    public void handleDeletePod(Long clusterId, String payload) {
        DeletePodVO deletePodVO = JsonHelper.unmarshalByJackson(payload, DeletePodVO.class);
        if (deletePodVO.getStatus().equals("failed")) {
            LOGGER.info("failed to delete pod {} namespace {}", deletePodVO.getPodName(), deletePodVO.getNamespace());
        } else {
            devopsEnvPodService.baseDeleteByName(deletePodVO.getPodName(), deletePodVO.getNamespace());
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryByClusterIdAndCode(clusterId, deletePodVO.getNamespace());
            devopsEnvResourceService.deleteByEnvIdAndKindAndName(devopsEnvironmentDTO.getId(), ResourceType.POD.getType(), deletePodVO.getPodName());
        }
    }

    @Override
    public void operatePodCount(String key, String payload, Long clusterId, boolean success) {
        OperationPodPayload operationPodPayload = JsonHelper.unmarshalByJackson(payload, OperationPodPayload.class);
        if (ObjectUtils.isEmpty(operationPodPayload.getCommandId())) {
            return;
        }
        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(Long.parseLong(operationPodPayload.getCommandId()));
        if (success) {
            devopsEnvCommandDTO.setStatus("success");
            AppServiceInstanceDTO instanceDTO = appServiceInstanceService.baseQuery(devopsEnvCommandDTO.getObjectId());
            if (instanceDTO != null) {
                instanceDTO.setEffectCommandId(devopsEnvCommandDTO.getId());
                appServiceInstanceService.baseUpdate(instanceDTO);
            }
        } else {
            devopsEnvCommandDTO.setStatus("failed");
            devopsEnvCommandDTO.setError(operationPodPayload.getMsg());
        }
        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
    }

    @Transactional(rollbackFor = Exception.class, isolation = READ_COMMITTED)
    @Override
    public void gitOpsSyncEvent(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        LOGGER.info("env {} receive git ops msg :\n{}", envId, msg);
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
                        case PERSISTENT_VOLUME_KIND:
                            syncPersistentVolume(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        case PERSISTENT_VOLUME_CLAIM_KIND:
                            syncPersistentVolumeClaim(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        case DEPLOYMENT:
                            syncDeployment(objects[0], envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        case JOB:
                        case DAEMONSET:
                        case CRON_JOB:
                        case STATEFULSET:
                            syncWorkload(objects[0], envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                        default:
                            syncCustom(envId, errorDevopsFiles, resourceCommitVO, objects);
                            break;
                    }
                });
    }

    private void syncDeployment(String type, Long envId, List<DevopsEnvFileErrorDTO> envFileErrorFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;

        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.baseQueryByEnvIdAndName(envId, objects[1]);
        if (devopsDeploymentDTO == null) {
            LOGGER.info("Non workload resource with envId: {}, kind: {}, name: {}", envId, objects[0], objects[1]);
            return;
        }
        Long resourceId = devopsDeploymentDTO.getId();
        Long commandId = devopsDeploymentDTO.getCommandId();

        if (resourceId == null || commandId == null) {
            LOGGER.info("Non workload resource with envId: {}, kind: {}, name: {}", envId, objects[0], objects[1]);
            return;
        }

        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, resourceId, type);
        if (Boolean.TRUE.equals(updateEnvCommandStatus(resourceCommitVO, commandId, devopsEnvFileResourceDTO,
                objects[0], objects[1], CommandStatus.SUCCESS.getStatus(), envFileErrorFiles))) {
            // 更新状态为failed
            devopsDeploymentDTO.setStatus(InstanceStatus.FAILED.getStatus());
            devopsDeploymentService.baseUpdate(devopsDeploymentDTO);
        }
    }

    private void syncPersistentVolume(Long envId, List<DevopsEnvFileErrorDTO> envFileErrorFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        // 外层已经判断了环境id一定是从数据库中查出来的，所以不可能为空，不考虑处理的同时用户删除环境的情况
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);

        DevopsPvDTO devopsPvDTO = devopsPvService.queryByEnvIdAndName(envId, objects[1]);
        if (devopsPvDTO == null) {
            if (devopsEnvironmentDTO != null
                    && EnvironmentType.USER.getValue().equals(devopsEnvironmentDTO.getType())) {
                // 目前用户环境是支持PVC而不支持PV，如果PV在非系统环境创建，应该被视为自定义资源
                syncCustom(envId, envFileErrorFiles, resourceCommitVO, objects);
            } else {
                LOGGER.warn("The devopsPvDTO is null with envId: {} and name: {}.", envId, objects[1]);
            }
            return;
        }

        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsPvDTO.getId(), ObjectType.PERSISTENTVOLUME.getType());
        if (updateEnvCommandStatus(resourceCommitVO,
                devopsPvDTO.getCommandId(),
                devopsEnvFileResourceDTO,
                PERSISTENT_VOLUME_KIND,
                devopsPvDTO.getName(),
                CommandStatus.SUCCESS.getStatus(),
                envFileErrorFiles)) {
            if (Objects.equals(devopsPvDTO.getStatus(), PvStatus.OPERATING.getStatus())) {
                devopsPvMapper.updateStatusById(devopsPvDTO.getId(), PvStatus.FAILED.getStatus());
            }
        }
    }

    private void syncPersistentVolumeClaim(Long envId, List<DevopsEnvFileErrorDTO> envFileErrorFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsPvcDTO devopsPvcDTO = devopsPvcService.queryByEnvIdAndName(envId, objects[1]);

        // 兼容0.20版本之前的作为自定义资源的PVC
        if (devopsPvcDTO == null) {
            try {
                if (devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, ResourceType.PERSISTENT_VOLUME_CLAIM.getType(), objects[1]) != null) {
                    syncCustom(envId, envFileErrorFiles, resourceCommitVO, objects);
                }
            } catch (Exception e) {
                LOGGER.info("Exception occurred when process resource {} as custom", objects[1]);
                LOGGER.info("The exception is {}", e);
            }
            return;
        }

        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsPvcDTO.getId(), ObjectType.PERSISTENTVOLUMECLAIM.getType());
        if (updateEnvCommandStatus(resourceCommitVO,
                devopsPvcDTO.getCommandId(),
                devopsEnvFileResourceDTO,
                PERSISTENT_VOLUME_CLAIM_KIND,
                devopsPvcDTO.getName(),
                CommandStatus.SUCCESS.getStatus(),
                envFileErrorFiles)) {
            if (Objects.equals(devopsPvcDTO.getStatus(), PvcStatus.OPERATING.getStatus())) {
                devopsPvcMapper.updateStatusById(devopsPvcDTO.getId(), PvcStatus.FAILED.getStatus());
                //创建PVC资源失败，发送失败通知JSON
                DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsPvcDTO.getEnvId());
                sendNotificationService.sendWhenPVCResource(devopsPvcDTO, devopsEnvironmentDTO, SendSettingEnum.CREATE_RESOURCE_FAILED.value());
            } else {
                //创建PVC资源成功发送 成功通知 JSON
                DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsPvcDTO.getEnvId());
                sendNotificationService.sendWhenPVCResource(devopsPvcDTO, devopsEnvironmentDTO, SendSettingEnum.CREATE_RESOURCE.value());
            }
        }
    }

    private void syncSecret(Long envId, List<DevopsEnvFileErrorDTO> envFileErrorFiles, ResourceCommitVO resourceCommitVO,
                            String[] objects) {

        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        DevopsSecretDTO devopsSecretDTO = devopsSecretService.baseQueryByEnvIdAndName(envId, objects[1]);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsSecretDTO.getId(), ObjectType.SECRET.getType());
        updateEnvCommandStatus(resourceCommitVO, devopsSecretDTO.getCommandId(), devopsEnvFileResourceDTO,
                SECRET_KIND, devopsSecretDTO.getName(), CommandStatus.SUCCESS.getStatus(), envFileErrorFiles);

        sendNotificationService.sendWhenSecret(devopsSecretDTO, SendSettingEnum.CREATE_RESOURCE.value());
    }


    private void syncCustom(Long envId, List<DevopsEnvFileErrorDTO> envFileErrorFiles, ResourceCommitVO resourceCommitVO,
                            String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, objects[0], objects[1]);
        if (devopsCustomizeResourceDTO == null) {
            LOGGER.info("Non custom resource with envId: {}, kind: {}, name: {}", envId, objects[0], objects[1]);
            return;
        }
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsCustomizeResourceDTO.getId(), ObjectType.CUSTOM.getType());
        updateEnvCommandStatus(resourceCommitVO, devopsCustomizeResourceDTO.getCommandId(), devopsEnvFileResourceDTO,
                objects[0], devopsCustomizeResourceDTO.getName(), CommandStatus.SUCCESS.getStatus(), envFileErrorFiles);
    }


    private void syncWorkload(String type, Long envId, List<DevopsEnvFileErrorDTO> envFileErrorFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        Long resourceId = null;
        Long commandId = null;
        switch (type) {
            case DEPLOYMENT:
                DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.baseQueryByEnvIdAndName(envId, objects[1]);
                if (devopsDeploymentDTO == null) {
                    LOGGER.info("Non workload resource with envId: {}, kind: {}, name: {}", envId, objects[0], objects[1]);
                    return;
                }
                resourceId = devopsDeploymentDTO.getId();
                commandId = devopsDeploymentDTO.getCommandId();
                break;
            case STATEFULSET:
                DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetService.baseQueryByEnvIdAndName(envId, objects[1]);
                if (devopsStatefulSetDTO == null) {
                    LOGGER.info("Non workload resource with envId: {}, kind: {}, name: {}", envId, objects[0], objects[1]);
                    return;
                }
                resourceId = devopsStatefulSetDTO.getId();
                commandId = devopsStatefulSetDTO.getCommandId();
                break;
            case JOB:
                DevopsJobDTO devopsJobDTO = devopsJobService.baseQueryByEnvIdAndName(envId, objects[1]);
                if (devopsJobDTO == null) {
                    LOGGER.info("Non workload resource with envId: {}, kind: {}, name: {}", envId, objects[0], objects[1]);
                    return;
                }
                resourceId = devopsJobDTO.getId();
                commandId = devopsJobDTO.getCommandId();
                break;
            case DAEMONSET:
                DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetService.baseQueryByEnvIdAndName(envId, objects[1]);
                if (devopsDaemonSetDTO == null) {
                    LOGGER.info("Non workload resource with envId: {}, kind: {}, name: {}", envId, objects[0], objects[1]);
                    return;
                }
                resourceId = devopsDaemonSetDTO.getId();
                commandId = devopsDaemonSetDTO.getCommandId();
                break;
            case CRON_JOB:
                DevopsCronJobDTO devopsCronJobDTO = devopsCronJobService.baseQueryByEnvIdAndName(envId, objects[1]);
                if (devopsCronJobDTO == null) {
                    LOGGER.info("Non workload resource with envId: {}, kind: {}, name: {}", envId, objects[0], objects[1]);
                    return;
                }
                resourceId = devopsCronJobDTO.getId();
                commandId = devopsCronJobDTO.getCommandId();
                break;
        }

        if (resourceId == null || commandId == null) {
            LOGGER.info("Non workload resource with envId: {}, kind: {}, name: {}", envId, objects[0], objects[1]);
            return;
        }

        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, resourceId, type);
        updateEnvCommandStatus(resourceCommitVO, commandId, devopsEnvFileResourceDTO,
                objects[0], objects[1], CommandStatus.SUCCESS.getStatus(), envFileErrorFiles);
    }

    private void syncCetificate(Long envId, List<DevopsEnvFileErrorDTO> errorDevopsFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        CertificationDTO certificationDTO = certificationService
                .baseQueryByEnvAndName(envId, objects[1]);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(
                        envId, certificationDTO.getId(), "Certificate");
        CertificationStatus updateStatus;
        if (updateEnvCommandStatus(resourceCommitVO, certificationDTO.getCommandId(),
                devopsEnvFileResourceDTO, CERTIFICATE_KIND, certificationDTO.getName(),
                null, errorDevopsFiles)) {
            // 发送资源创建失败通知
            sendNotificationService.sendWhenCertificationCreationFailure(certificationDTO, certificationDTO.getCreatedBy(), certificationDTO.getCommandId());
            updateStatus = CertificationStatus.FAILED;
        } else {
            // 如果此时证书的状态不是 active, 就更新为 applying
            updateStatus = CertificationStatus.APPLYING;
        }
        int updated = certificationService.updateStatusIfOperating(certificationDTO.getId(), updateStatus);
        LOGGER.info("GitOps sync event: update certification with id {} to status {}, result {}", certificationDTO.getId(), updateStatus.getStatus(), updated);
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
            // 发送网络资源创建失败通知
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsEnvFileResourceDTO.getEnvId());
            sendNotificationService.sendWhenServiceCreationFailure(devopsServiceDTO, devopsServiceDTO.getCreatedBy(), devopsEnvironmentDTO, devopsServiceDTO.getCommandId());
        } else {
            devopsServiceDTO.setStatus(ServiceStatus.RUNNING.getStatus());
            // 网络资源创建成功后，发送成功的webhook json
            DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceDTO.getEnvId());
            sendNotificationService.sendWhenServiceCreationSuccessOrDelete(devopsServiceDTO, devopsEnvironmentDTO, SendSettingEnum.CREATE_RESOURCE.value());
        }
        devopsServiceService.updateStatus(devopsServiceDTO);
    }

    private void syncIngress(Long envId, List<DevopsEnvFileErrorDTO> errorDevopsFiles, ResourceCommitVO resourceCommitVO, String[] objects) {
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO;
        DevopsIngressDTO devopsIngressDTO = devopsIngressService
                .baseCheckByEnvAndName(envId, objects[1]);
        devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(envId, devopsIngressDTO.getId(), "Ingress");
        if (updateEnvCommandStatus(resourceCommitVO, devopsIngressDTO.getCommandId(),
                devopsEnvFileResourceDTO, INGRESS_KIND, devopsIngressDTO.getName(), CommandStatus.SUCCESS.getStatus(), errorDevopsFiles)) {
            devopsIngressService.updateStatus(envId, devopsIngressDTO.getName(), IngressStatus.FAILED.getStatus());
            // 发送资源创建失败通知
            sendNotificationService.sendWhenIngressCreationFailure(devopsIngressDTO, devopsIngressDTO.getCreatedBy(), devopsIngressDTO.getCommandId());
        } else {
            devopsIngressService.updateStatus(envId, devopsIngressDTO.getName(), IngressStatus.RUNNING.getStatus());
            //发送域名创建成功的webhook
            sendNotificationService.sendWhenIngressSuccessOrDelete(devopsIngressDTO, SendSettingEnum.CREATE_RESOURCE.value());
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
            // 屏蔽运行时的实例错误信息
            DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService
                    .baseQueryByObject(ObjectType.INSTANCE.getType(), appServiceInstanceDTO.getId());
            if (!appServiceInstanceDTO.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
                appServiceInstanceDTO.setStatus(InstanceStatus.FAILED.getStatus());
                appServiceInstanceService.baseUpdate(appServiceInstanceDTO);
                instanceDeployFailed(appServiceInstanceDTO.getId(), devopsEnvCommandDTO.getId());
                // 发送资源创建失败通知
                sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, appServiceInstanceDTO.getStatus());
            }
            if (InstanceStatus.RUNNING.getStatus().equals(appServiceInstanceDTO.getStatus())) {
                // 发送成功通知
                sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, appServiceInstanceDTO.getStatus());
            }
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

        //进入此方法已经表示配置创建成功，所以发送成功的webhook json
        sendNotificationService.sendWhenConfigMap(devopsConfigMapDTO, SendSettingEnum.CREATE_RESOURCE.value());


    }

    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.DEVOPS_DEPLOY_FAILED,
            description = "实例部署失败",
            inputSchemaClass = DevopsDeployFailedVO.class)
    private void instanceDeployFailed(Long instanceId, Long commandId) {
        DevopsDeployFailedVO devopsDeployFailedVO = new DevopsDeployFailedVO(instanceId, commandId);
        producer.applyAndReturn(
                        StartSagaBuilder
                                .newBuilder()
                                .withLevel(ResourceLevel.PROJECT)
                                .withRefId(instanceId.toString())
                                .withRefType("instance")
                                .withSagaCode(SagaTopicCodeConstants.DEVOPS_DEPLOY_FAILED),
                        builder -> builder
                                .withPayloadAndSerialize(devopsDeployFailedVO))
                .withRefId(instanceId.toString());
    }

    private List<DevopsEnvFileErrorDTO> getEnvFileErrors(Long envId, GitOpsSyncDTO gitOpsSyncDTO, DevopsEnvironmentDTO devopsEnvironmentDTO) {
        List<DevopsEnvFileErrorDTO> errorDevopsFiles = new ArrayList<>();
        if (gitOpsSyncDTO.getMetadata().getErrors() != null) {
            gitOpsSyncDTO.getMetadata().getErrors().forEach(error -> {
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

        // 这种情况是用户界面上主动停止init-job，agent将Reversion设置成了很大
        if (oldDevopsEnvResourceDTO.getName().endsWith("init-db")) {
            if (devopsEnvResourceDTO.getKind().equals("Job") && oldDevopsEnvResourceDTO.getReversion() < devopsEnvResourceDTO.getReversion()) {
                oldDevopsEnvResourceDTO.setReversion(devopsEnvResourceDTO.getReversion());
                devopsEnvResourceDetailDTO.setId(oldDevopsEnvResourceDTO.getResourceDetailId());
                devopsEnvResourceService.baseUpdate(oldDevopsEnvResourceDTO);
                devopsEnvResourceDetailService.baseUpdate(devopsEnvResourceDetailDTO);
            }
        } else {
            if (!oldDevopsEnvResourceDTO.getReversion().equals(devopsEnvResourceDTO.getReversion())) {
                oldDevopsEnvResourceDTO.setReversion(devopsEnvResourceDTO.getReversion());
                devopsEnvResourceDetailDTO.setId(oldDevopsEnvResourceDTO.getResourceDetailId());
                devopsEnvResourceService.baseUpdate(oldDevopsEnvResourceDTO);
                devopsEnvResourceDetailService.baseUpdate(devopsEnvResourceDetailDTO);
            }
        }
    }

    private void installResource(List<Resource> resources, AppServiceInstanceDTO appServiceInstanceDTO) {
        try {
            if (CollectionUtils.isEmpty(resources)) {
                // 可能为空的情况是prometheus的资源数据过大(80M), 所以agent处理将resource字段设置为null
                LOGGER.info("InstallResource: resource empty for instance with code: {}", appServiceInstanceDTO.getCode());
                return;
            }
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
                // 如果需要保存对应类型的资源信息，则保存。
                ChartResourceOperatorService chartResourceOperatorService = chartResourceOperator.getOperatorMap().get(resource.getKind());
                if (chartResourceOperatorService != null) {
                    chartResourceOperatorService.saveOrUpdateChartResource(resource.getObject(), appServiceInstanceDTO);
                }
                // 保存应用异常数据（采集监控报表数据）
                appExceptionRecordService.createOrUpdateExceptionRecord(resource.getKind(), resource.getObject(), appServiceInstanceDTO);


            }
        } catch (Exception e) {
            LOGGER.info("Exception occurred when processing installResource. It is: ", e);
            LOGGER.info("And the resources is : {}", resources);
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
        devopsEnvPodDTO.setEnvId(appServiceInstanceDTO.getEnvId());
        devopsEnvPodDTO.setResourceVersion(resourceVersion);
        devopsEnvPodDTO.setNamespace(v1Pod.getMetadata().getNamespace());
        devopsEnvPodDTO.setReady(getReadyValue(status, v1Pod));
        devopsEnvPodDTO.setInstanceId(appServiceInstanceDTO.getId());
        devopsEnvPodDTO.setNodeName(v1Pod.getSpec().getNodeName());
        devopsEnvPodDTO.setRestartCount(K8sUtil.getRestartCountForPod(v1Pod));

        devopsEnvPodService.baseCreate(devopsEnvPodDTO);
    }

    private void insertDevopsCommandEvent(Long envId, Event event, String type, PodSourceEnums source) {
        DevopsEnvResourceDTO devopsEnvResourceDTO = devopsEnvResourceService
                .baseQueryByKindAndName(envId, event.getInvolvedObject().getKind(), event.getInvolvedObject().getName());

        if (devopsEnvResourceDTO == null) {
            // TODO 0.21版本修复Agent没有过滤非平台的Pod和Job的问题
            // logger.warn("DevopsEnvResourceDTO is null with involved object kind {} and involved object name {}", event.getInvolvedObject().getKind(), event.getInvolvedObject().getName());
            return;
        }

        try {
            if (StringUtils.isEmpty(event.getCommitSha())) {
                LOGGER.warn("The commit sha of event is unexpectedly empty...");
                return;
            }

            DevopsEnvCommandDTO devopsEnvCommandDTO;
            if (PodSourceEnums.HELM.equals(source)) {
                devopsEnvCommandDTO = devopsEnvCommandService.queryByInstanceIdAndCommitSha(devopsEnvResourceDTO.getInstanceId(), event.getCommitSha());
            } else {
                DevopsEnvPodDTO devopsEnvPodDTO = devopsEnvPodService.baseQueryByEnvIdAndName(devopsEnvResourceDTO.getEnvId(), devopsEnvResourceDTO.getName());
                Long workloadId = workloadService.getWorkloadId(devopsEnvResourceDTO.getEnvId(), devopsEnvPodDTO.getOwnerRefName(), devopsEnvPodDTO.getOwnerRefKind());
                devopsEnvCommandDTO = devopsEnvCommandService.queryByWorkloadTypeAndObjectIdAndCommitSha(devopsEnvPodDTO.getOwnerRefKind(), workloadId, event.getCommitSha());
            }
            if (devopsEnvCommandDTO == null) {
                LOGGER.info("InsertCommandEvent: EnvCommand is not found. InstanceId={}, commitSha={}", devopsEnvResourceDTO.getInstanceId(), event.getCommitSha());
                return;
            }

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
            LOGGER.warn("Exception occurred when calling insertDevopsCommandEvent(), it is: ", e);
        }
    }


    @Override
    public List<AppServiceDTO> getApplication(String appServiceName, Long projectId, Long orgId) {
        List<AppServiceDTO> applications = new ArrayList<>();
        AppServiceDTO applicationDTO = appServiceService
                .baseQueryByCode(appServiceName, projectId);
        if (applicationDTO != null) {
            applications.add(applicationDTO);
        }

        Long organizationId = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId).getOrganizationId();
        List<Long> appServiceIds = new ArrayList<>();
        baseServiceClientOperator.listIamProjectByOrgId(organizationId).forEach(pro -> {
            AppServiceDTO appServiceDTO = new AppServiceDTO();
            appServiceDTO.setProjectId(pro.getId());
            appServiceIds.addAll(appServiceMapper.select(appServiceDTO).stream().map(AppServiceDTO::getId).collect(Collectors.toList()));
        });
        applications.addAll(appServiceMapper.listShareApplicationService(appServiceIds, projectId, null, null));
        return applications;
    }

    @Override
    public void resourceStatusSyncEvent(String key, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        List<Command> commands = getCommandsToSync(envId);

        String namespace = GitOpsUtil.getEnvNamespace(devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getType());
        agentCommandService.gitopsSyncCommandStatus(clusterId, namespace, envId, commands);
    }

    /**
     * 获取环境下最近三分钟的还在处理中的command
     *
     * @param envId 环境id
     * @return command列表
     */
    private List<Command> getCommandsToSync(Long envId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);

        // 获取三分钟以前的时间
        Date threeMinutesBefore = new Date(System.currentTimeMillis() - THREE_MINUTE_MILLISECONDS);
        String dateString = simpleDateFormat.format(threeMinutesBefore);

        return devopsEnvCommandService.listCommandsToSync(envId, dateString);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resourceStatusSync(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
            return;
        }

        LOGGER.debug("sync command status result: {}.", msg);

        Map<Long, Command> syncCommandMap = JSONArray.parseArray(msg, Command.class)
                .stream()
                .filter(c -> c != null && c.getId() != null)
                .collect(Collectors.toMap(Command::getId, Function.identity()));

        List<Command> oldCommands = getCommandsToSync(envId);
        oldCommands.stream()
                .filter(oldCommand -> oldCommand.getId() != null)
                .forEach(oldCommand -> {
                    Command newCommand = syncCommandMap.get(oldCommand.getId());
                    if (newCommand == null) {
                        return;
                    }

                    // 查询command
                    DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(oldCommand.getId());
                    // 对比两边command的sha值是否相等
                    if (newCommand.getCommit() != null
                            && Objects.equals(oldCommand.getCommit(), newCommand.getCommit())) {

                        devopsEnvCommandDTO.setStatus(CommandStatus.SUCCESS.getStatus());
                        if (ObjectType.CONFIGMAP.getType().equals(devopsEnvCommandDTO.getObject())) {
                            DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService.baseQueryById(devopsEnvCommandDTO.getObjectId());
                            sendNotificationService.sendWhenConfigMap(devopsConfigMapDTO, SendSettingEnum.CREATE_RESOURCE.value());
                        }
                        if (ObjectType.SECRET.getType().equals(devopsEnvCommandDTO.getObject())) {
                            DevopsSecretDTO devopsSecretDTO = devopsSecretMapper.selectByPrimaryKey(devopsEnvCommandDTO.getObjectId());
                            sendNotificationService.sendWhenSecret(devopsSecretDTO, SendSettingEnum.CREATE_RESOURCE.value());
                        }
                        updateResourceStatus(envId, devopsEnvCommandDTO,
                                InstanceStatus.RUNNING,
                                ServiceStatus.RUNNING,
                                IngressStatus.RUNNING,
                                CertificationStatus.ACTIVE,
                                PvStatus.forValue(newCommand.getResourceStatus()),
                                PvcStatus.forValue(newCommand.getResourceStatus()),
                                InstanceStatus.RUNNING);
                    } else {
                        devopsEnvCommandDTO.setStatus(CommandStatus.FAILED.getStatus());
                        devopsEnvCommandDTO.setError("The deploy is time out!");
                        if (ObjectType.CONFIGMAP.getType().equals(devopsEnvCommandDTO.getObject())) {
                            DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService.baseQueryById(devopsEnvCommandDTO.getObjectId());
                            sendNotificationService.sendWhenConfigMap(devopsConfigMapDTO, SendSettingEnum.CREATE_RESOURCE_FAILED.value());
                        }
                        if (ObjectType.SECRET.getType().equals(devopsEnvCommandDTO.getObject())) {
                            DevopsSecretDTO devopsSecretDTO = devopsSecretMapper.selectByPrimaryKey(devopsEnvCommandDTO.getObjectId());
                            sendNotificationService.sendWhenSecret(devopsSecretDTO, SendSettingEnum.CREATE_RESOURCE_FAILED.value());
                        }
                        updateResourceStatus(envId, devopsEnvCommandDTO,
                                InstanceStatus.FAILED,
                                ServiceStatus.FAILED,
                                IngressStatus.FAILED,
                                CertificationStatus.FAILED,
                                PvStatus.FAILED,
                                PvcStatus.FAILED,
                                InstanceStatus.FAILED);
                    }

                    devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
                });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handlerServiceCreateMessage(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
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
            LOGGER.info("Exception occurred when calling handlerServiceCreateMessage(). It is:", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void namespaceInfo(String msg, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        devopsClusterDTO.setNamespaces(msg);
        devopsClusterService.baseUpdate(null, devopsClusterDTO);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.TEST_POD_UPDATE_SAGA,
            description = "测试应用Pod升级(test pod update saga)", inputSchema = "{}")
    public void testPodUpdate(String key, String msg, Long clusterId) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);
        String status = K8sUtil.changePodStatus(v1Pod);
        LOGGER.debug("Test Pod UPDATE: key: {}, payload: {}", key, msg);
        if (status.equals("Running")) {
            PodUpdateVO podUpdateVO = new PodUpdateVO();
            Optional<V1Container> container = v1Pod.getSpec().getContainers().stream().filter(v1Container -> v1Container.getName().contains("automation-test")).findFirst();
            container.ifPresent(v1Container -> podUpdateVO.setConName(v1Container.getName()));
            podUpdateVO.setPodName(v1Pod.getMetadata().getName());
            podUpdateVO.setReleaseNames(KeyParseUtil.getReleaseName(key));
            podUpdateVO.setStatus(0L);

            // 将Pod Running的状态发送给敏捷组
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
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.TEST_JOB_LOG_SAGA,
            description = "测试Job日志(test job log saga)", inputSchema = "{}")
    public void testJobLog(String key, String msg, Long clusterId) {
        LOGGER.debug("Test JOB LOG SAGA: key: {}, payload: {}", key, msg);
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
        LOGGER.info(input);

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
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON, code = SagaTopicCodeConstants.TEST_STATUS_SAGA,
            description = "测试Release状态(test status saga)", inputSchema = "{}")
    public void getTestAppStatus(String key, String msg, Long clusterId) {
        LOGGER.debug("Test STATUS SAGA: key: {}, payload: {}", key, msg);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handleCertManagerInfo(AgentMsgVO agentMsgVO, Long clusterId) {
        DevopsClusterResourceDTO devopsClusterResourceDTO = devopsClusterResourceService.queryByClusterIdAndType(clusterId, ClusterResourceType.CERTMANAGER.getType());
        CertManagerReleaseInfo certManagerReleaseInfo = json.deserialize(agentMsgVO.getPayload(), CertManagerReleaseInfo.class);
        //如果集群安装了 cert_manager 而数据库没有数据就插入数据库
        if (Objects.isNull(devopsClusterResourceDTO) && CertManagerConstants.RUNNING.equalsIgnoreCase(certManagerReleaseInfo.getStatus())) {
            DevopsClusterResourceDTO clusterResourceDTO = new DevopsClusterResourceDTO();
            clusterResourceDTO.setType(ClusterResourceType.CERTMANAGER.getType());
            clusterResourceDTO.setClusterId(clusterId);

            DevopsCertManagerRecordDTO devopsCertManagerRecordDTO = new DevopsCertManagerRecordDTO();
            devopsCertManagerRecordDTO.setStatus(ClusterResourceStatus.AVAILABLE.getStatus().toLowerCase());
            devopsCertManagerRecordMapper.insertSelective(devopsCertManagerRecordDTO);
            //记录CertManager的信息
            DevopsCertManagerDTO devopsCertManagerDTO = new DevopsCertManagerDTO();
            devopsCertManagerDTO.setNamespace(certManagerReleaseInfo.getNamespace());
            devopsCertManagerDTO.setChartVersion(certManagerReleaseInfo.getChartVersion());
            devopsCertManagerDTO.setReleaseName(certManagerReleaseInfo.getReleaseName());
            devopsCertManagerMapper.insertSelective(devopsCertManagerDTO);
            // 插入数据
            clusterResourceDTO.setObjectId(devopsCertManagerRecordDTO.getId());
            clusterResourceDTO.setClusterId(clusterId);
            clusterResourceDTO.setOperate(ClusterResourceOperateType.INSTALL.getType());
            clusterResourceDTO.setConfigId(devopsCertManagerDTO.getId());
            devopsClusterResourceService.baseCreate(clusterResourceDTO);
        } else if (!ObjectUtils.isEmpty(devopsClusterResourceDTO)) {
            //安装返回,如果不是running状态就是不可用
            if (ClusterResourceOperateType.INSTALL.getType().equals(devopsClusterResourceDTO.getOperate())) {
                if (CertManagerConstants.RUNNING.equalsIgnoreCase(certManagerReleaseInfo.getStatus())) {
                    devopsClusterResourceService.updateCertMangerStatus(clusterId, ClusterResourceStatus.AVAILABLE.getStatus().toLowerCase(), null);
                } else {
                    devopsClusterResourceService.updateCertMangerStatus(clusterId, ClusterResourceStatus.DISABLED.getStatus().toLowerCase(), agentMsgVO.getPayload());
                    //安装cert-manager失败返回
                    sendNotificationService.sendWhenResourceInstallFailed(devopsClusterResourceDTO, SendSettingEnum.RESOURCE_INSTALLFAILED.value(), ClusterResourceType.CERTMANAGER.getType(), clusterId, agentMsgVO.getPayload());
                }
            }
            //卸载返回
            if (ClusterResourceOperateType.UNINSTALL.getType().equals(devopsClusterResourceDTO.getOperate())) {
                if (CertManagerConstants.DELETED.equalsIgnoreCase(certManagerReleaseInfo.getStatus())) {
                    devopsClusterResourceService.unloadCertManager(clusterId);
                } else {
                    devopsClusterResourceService.updateCertMangerStatus(clusterId, ClusterResourceStatus.DISABLED.getStatus().toLowerCase(), agentMsgVO.getPayload());
                }
            }
        }
    }


    private void updateResourceStatus(Long envId,
                                      DevopsEnvCommandDTO devopsEnvCommandDTO,
                                      InstanceStatus instanceStatus,
                                      ServiceStatus serviceStatus,
                                      IngressStatus ingressStatus,
                                      CertificationStatus certificationStatus,
                                      PvStatus pvStatus,
                                      PvcStatus pvcStatus,
                                      InstanceStatus deploymentStatus) {
        switch (ObjectType.forValue(devopsEnvCommandDTO.getObject())) {
            case INSTANCE:
                AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQuery(devopsEnvCommandDTO.getObjectId());
                if (appServiceInstanceDTO != null
                        && !InstanceStatus.RUNNING.getStatus().equals(appServiceInstanceDTO.getStatus())) {
                    appServiceInstanceDTO.setStatus(instanceStatus.getStatus());
                    appServiceInstanceService.updateStatus(appServiceInstanceDTO);
                    // 发送资源创建失败通知
                    LOGGER.debug("Sending instance notices: The instance status is {}, the command type is {}", instanceStatus.getStatus(), devopsEnvCommandDTO.getCommandType());
                    if (InstanceStatus.FAILED == instanceStatus
                            && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
                        LOGGER.debug("Sending instance notices: env id: {}, instance code {}, createdby: {}", appServiceInstanceDTO.getEnvId(), appServiceInstanceDTO.getCode(), appServiceInstanceDTO.getCreatedBy());
                        sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, appServiceInstanceDTO.getStatus());
                    }
                    if (InstanceStatus.RUNNING == instanceStatus
                            && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
                        LOGGER.debug("Sending instance notices: env id: {}, instance code {}, createdby: {}", appServiceInstanceDTO.getEnvId(), appServiceInstanceDTO.getCode(), appServiceInstanceDTO.getCreatedBy());
                        sendNotificationService.sendInstanceStatusUpdate(appServiceInstanceDTO, devopsEnvCommandDTO, appServiceInstanceDTO.getStatus());
                    }
                }
                break;
            case SERVICE:
                DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQuery(devopsEnvCommandDTO.getObjectId());
                devopsServiceDTO.setStatus(serviceStatus.getStatus());
                devopsServiceService.updateStatus(devopsServiceDTO);
                // 发送资源创建失败通知
                if (ServiceStatus.FAILED == serviceStatus
                        && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsEnvCommandDTO.getEnvId());
                    sendNotificationService.sendWhenServiceCreationFailure(devopsServiceDTO, devopsServiceDTO.getCreatedBy(), devopsEnvironmentDTO, null);
                }
                //如果成功发送成功通知 webhook json
                DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(devopsServiceDTO.getEnvId());
                sendNotificationService.sendWhenServiceCreationSuccessOrDelete(devopsServiceDTO, devopsEnvironmentDTO, SendSettingEnum.CREATE_RESOURCE.value());
                break;
            case INGRESS:
                DevopsIngressDTO devopsIngressDTO = devopsIngressService.baseQuery(devopsEnvCommandDTO.getObjectId());
                devopsIngressService.updateStatus(envId, devopsIngressDTO.getName(), ingressStatus.getStatus());
                // 发送资源创建失败通知
                if (IngressStatus.FAILED == ingressStatus
                        && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
                    sendNotificationService.sendWhenIngressCreationFailure(devopsIngressDTO, devopsIngressDTO.getCreatedBy(), null);
                }
                if (IngressStatus.RUNNING == ingressStatus
                        && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
                    sendNotificationService.sendWhenIngressSuccessOrDelete(devopsIngressDTO, SendSettingEnum.CREATE_RESOURCE.value());
                }
                break;
            case CERTIFICATE:
                CertificationDTO certificationDTO = certificationService.baseQueryById(devopsEnvCommandDTO.getObjectId());
                certificationDTO.setStatus(certificationStatus.getStatus());
                certificationService.updateStatus(certificationDTO);
                // 发送资源创建失败通知
                if (CertificationStatus.FAILED == certificationStatus
                        && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
                    sendNotificationService.sendWhenCertificationCreationFailure(certificationDTO, certificationDTO.getCreatedBy(), null);
                }
                if (CertificationStatus.ACTIVE == certificationStatus
                        && CommandType.CREATE.getType().equals(devopsEnvCommandDTO.getCommandType())) {
                    //创建成功发送webhook json
                    sendNotificationService.sendWhenCertSuccessOrDelete(certificationDTO, SendSettingEnum.CREATE_RESOURCE.value());
                }
                break;
            case PERSISTENTVOLUME:
                if (pvStatus == null) {
                    LOGGER.warn("Command Sync: unexpected pv status null for resource {} with id {}", devopsEnvCommandDTO.getObject(), devopsEnvCommandDTO.getObjectId());
                    return;
                }
                DevopsPvDTO devopsPvDTO = devopsPvMapper.selectByPrimaryKey(devopsEnvCommandDTO.getObjectId());
                if (pvStatus != PvStatus.FAILED) {
                    devopsPvMapper.updateStatusById(devopsEnvCommandDTO.getObjectId(), pvStatus.getStatus());
                } else if (devopsPvDTO != null
                        && Objects.equals(devopsPvDTO.getStatus(), PvStatus.OPERATING.getStatus())) {
                    devopsPvMapper.updateStatusById(devopsEnvCommandDTO.getObjectId(), pvStatus.getStatus());
                }
                break;
            case PERSISTENTVOLUMECLAIM:
                if (pvStatus == null) {
                    LOGGER.warn("Command Sync: unexpected pv status null for resource {} with id {}", devopsEnvCommandDTO.getObject(), devopsEnvCommandDTO.getObjectId());
                    return;
                }
                DevopsPvcDTO devopsPvcDTO = devopsPvcMapper.selectByPrimaryKey(devopsEnvCommandDTO.getObjectId());
                if (pvcStatus != PvcStatus.FAILED) {
                    devopsPvcMapper.updateStatusById(devopsEnvCommandDTO.getObjectId(), pvcStatus.getStatus());
                    //PVC创建失败发送失败webhook json
                    DevopsEnvironmentDTO environmentDTO = devopsEnvironmentService.baseQueryById(devopsEnvCommandDTO.getEnvId());
                    sendNotificationService.sendWhenPVCResource(devopsPvcDTO, environmentDTO, SendSettingEnum.CREATE_RESOURCE_FAILED.value());
                } else if (devopsPvcDTO != null
                        && Objects.equals(devopsPvcDTO.getStatus(), PvcStatus.OPERATING.getStatus())) {
                    devopsPvcMapper.updateStatusById(devopsEnvCommandDTO.getObjectId(), pvcStatus.getStatus());
                    //创建PVC成功发送通知
                    DevopsEnvironmentDTO baseQueryById = devopsEnvironmentService.baseQueryById(devopsEnvCommandDTO.getEnvId());
                    sendNotificationService.sendWhenPVCResource(devopsPvcDTO, baseQueryById, SendSettingEnum.CREATE_RESOURCE.value());
                }
                break;
            case DEPLOYMENT:
                if (deploymentStatus == InstanceStatus.FAILED) {
                    DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.selectByPrimaryKey(devopsEnvCommandDTO.getObjectId());
                    devopsDeploymentDTO.setStatus(InstanceStatus.FAILED.getStatus());
                    devopsDeploymentService.baseUpdate(devopsDeploymentDTO);
                }
                break;
            case STATEFULSET:
            case JOB:
            case CRONJOB:
            case DAEMONSET:
                break;
            default:
                LOGGER.warn("Unexpected resource kind when syncing commands: {}", devopsEnvCommandDTO.getObject());
                break;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void certIssued(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
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
                //证书资源创建成功，发送webhook json
                sendNotificationService.sendWhenCertSuccessOrDelete(certificationDTO, SendSettingEnum.CREATE_RESOURCE.value());
            }
        } catch (IOException e) {
            LOGGER.info(e.toString(), e);
        } catch (CertificateException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void certFailed(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
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
                certificationService.updateStatus(certificationDTO);
                // 发送创建失败通知
                sendNotificationService.sendWhenCertificationCreationFailure(certificationDTO, certificationDTO.getCreatedBy(), certificationService.baseQueryById(certificationDTO.getId()).getCommandId());
            }
        }

    }


    private Long getEnvId(String key, Long clusterId) {
        String namespace = KeyParseUtil.getNamespace(key);
        if (ObjectUtils.isEmpty(namespace)) {
            return null;
        }

        // choerodon namespace对应的环境的code并不是choerodon，要进行特殊处理
        if (GitOpsConstants.SYSTEM_NAMESPACE.equals(namespace)) {
            DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
            if (devopsClusterDTO == null) {
                LogUtil.loggerWarnObjectNullWithId("Cluster", clusterId, LOGGER);
                return null;
            }
            namespace = GitOpsUtil.getSystemEnvCode(devopsClusterDTO.getCode());
        }

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryByClusterIdAndCode(clusterId, namespace);
        if (devopsEnvironmentDTO == null) {
            LOGGER.warn("Environment with cluster id {} and code {} is null.", clusterId, namespace);
            return null;
        }
        return devopsEnvironmentDTO.getId();
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
                    podMetricsRedisInfoVO.setCpu(((Double) Math.ceil(cpu[0] / (1000 * 1000))).longValue() + "m");
                }
                if (memory[0] != 0L) {
                    podMetricsRedisInfoVO.setMemory(((Double) Math.floor(memory[0] / 1024)).longValue() + "Mi");
                }
                podMetricsRedisInfoVOS.add(podMetricsRedisInfoVO);
            });
            agentPodService.handleRealTimePodData(podMetricsRedisInfoVOS);
        }
    }

    @Override
    public void handleClusterInfo(AgentMsgVO msg) {
        Long clusterId = TypeUtil.objToLong(msg.getClusterId());
        // 应该要有 version, namespaces, pods, nodes字段
        ClusterSummaryInfoVO clusterSummaryInfoVO = JSONObject.parseObject(msg.getPayload(), ClusterSummaryInfoVO.class);
        devopsClusterService.saveClusterSummaryInfo(clusterId, clusterSummaryInfoVO);

        // 更新agentPodName
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        devopsClusterDTO.setPodName(ObjectUtils.isEmpty(clusterSummaryInfoVO.getAgentPodName()) ? null : clusterSummaryInfoVO.getAgentPodName());
        devopsClusterDTO.setNamespace(ObjectUtils.isEmpty(clusterSummaryInfoVO.getAgentNamespace()) ? null : clusterSummaryInfoVO.getAgentNamespace());
        devopsClusterService.baseUpdate(devopsClusterDTO.getProjectId(), devopsClusterDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handleConfigUpdate(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            LOGGER.info(ENV_NOT_EXIST, KeyParseUtil.getNamespace(key));
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

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public void operateDockerRegistrySecretResp(String key, String result, Long clusterId) {
        String namespace = KeyParseUtil.getNamespace(key);
        String resourceName = KeyParseUtil.getResourceName(key);
        if (clusterId == null || namespace == null || resourceName == null) {
            LOGGER.info("Bad response for registry secret: clusterId: {}, namespace: {}, resourceName: {}", clusterId, namespace, resourceName);
            return;
        }
        DevopsRegistrySecretDTO devopsRegistrySecretDTO = devopsRegistrySecretService.baseQueryByClusterAndNamespaceAndName(clusterId, namespace, resourceName);
        if (devopsRegistrySecretDTO == null) {
            LOGGER.info("Registry-secret with name {} wasn't found. The clusterId is {}, the namespace is {}", resourceName, clusterId, namespace);
            return;
        }
        if (result.equals("failed")) {
            devopsRegistrySecretService.baseUpdateStatus(devopsRegistrySecretDTO.getId(), false);
        } else {
            devopsRegistrySecretService.baseUpdateStatus(devopsRegistrySecretDTO.getId(), true);
        }
    }
}

