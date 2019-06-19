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
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.app.service.ClusterNodeInfoService;
import io.choerodon.devops.app.service.DeployMsgHandlerService;
import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.factory.DevopsInstanceResourceFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.*;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.*;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.devops.infra.mapper.ApplicationMarketMapper;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.process.SocketMsgDispatcher;
import io.choerodon.websocket.tool.KeyParseTool;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Zenger on 2018/4/17.
 */
@Service
public class DeployMsgHandlerServiceImpl implements DeployMsgHandlerService {

    public static final String CONFIG_MAP_PREFIX = "configMap-";
    public static final String CONFIGMAP = "ConfigMap";
    public static final String CREATE_TYPE = "create";
    public static final Long ADMIN = 1L;
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
    private static final Logger logger = LoggerFactory.getLogger(DeployMsgHandlerServiceImpl.class);
    private static final String RESOURCE_VERSION = "resourceVersion";
    private static final String INIT_AGENT = "init_agent";
    private static final String ENV_NOT_EXIST = "env not exists: {}";
    private static JSON json = new JSON();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private ObjectMapper mapper = new ObjectMapper();
    private Gson gson = new Gson();

    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${agent.repoUrl}")
    private String agentRepoUrl;
    @Value("${agent.certManagerUrl}")
    private String certManagerUrl;


    @Autowired
    private DevopsEnvPodRepository devopsEnvPodRepository;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private DevopsEnvResourceRepository devopsEnvResourceRepository;
    @Autowired
    private DevopsEnvResourceDetailRepository devopsEnvResourceDetailRepository;
    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsEnvCommandLogRepository devopsEnvCommandLogRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    @Lazy
    private SocketMsgDispatcher socketMsgDispatcher;
    @Autowired
    private ApplicationMarketMapper applicationMarketMapper;
    @Autowired
    private ApplicationMarketRepository applicationMarketRepository;
    @Autowired
    private DevopsCommandEventRepository devopsCommandEventRepository;
    @Autowired
    private DevopsEnvCommandValueRepository commandValueRepository;
    @Autowired
    private DevopsEnvCommandLogRepository commandLogRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private DevopsEnvFileRepository devopsEnvFileRepository;
    @Autowired
    private DevopsEnvCommitRepository devopsEnvCommitRepository;
    @Autowired
    private DevopsEnvFileErrorRepository devopsEnvFileErrorRepository;
    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private DevopsSecretRepository devopsSecretRepository;
    @Autowired
    private DevopsClusterRepository devopsClusterRepository;
    @Autowired
    private DevopsClusterProPermissionRepository devopsClusterProPermissionRepository;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private DevopsConfigMapRepository devopsConfigMapRepository;
    @Autowired
    private ClusterNodeInfoService clusterNodeInfoService;
    @Autowired
    private DevopsRegistrySecretRepository devopsRegistrySecretRepository;
    @Autowired
    private DevopsConfigMapService devopsConfigMapService;


    public void handlerUpdatePodMessage(String key, String msg, Long envId) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);

        ApplicationInstanceE applicationInstanceE =
                applicationInstanceRepository.selectByCode(KeyParseTool.getReleaseName(key), envId);
        if (applicationInstanceE == null) {
            logger.info("instance not found");
            return;
        }
        DevopsEnvResourceE devopsEnvResourceE = new DevopsEnvResourceE();
        DevopsEnvResourceE newDevopsEnvResourceE =
                devopsEnvResourceRepository.queryResource(
                        applicationInstanceE.getId(),
                        null,
                        null,
                        KeyParseTool.getResourceType(key),
                        v1Pod.getMetadata().getName());
        DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
        devopsEnvResourceDetailE.setMessage(msg);
        devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
        devopsEnvResourceE.initDevopsEnvironmentE(envId);
        devopsEnvResourceE.setName(v1Pod.getMetadata().getName());
        devopsEnvResourceE.setReversion(TypeUtil.objToLong(v1Pod.getMetadata().getResourceVersion()));
        List<V1OwnerReference> v1OwnerReferences = v1Pod.getMetadata().getOwnerReferences();
        if (v1OwnerReferences == null || v1OwnerReferences.isEmpty()) {
            return;
        }
        if (v1OwnerReferences.get(0).getKind().equals(ResourceType.JOB.getType())) {
            return;
        }
        saveOrUpdateResource(devopsEnvResourceE,
                newDevopsEnvResourceE,
                devopsEnvResourceDetailE,
                applicationInstanceE);
        String status = K8sUtil.changePodStatus(v1Pod);
        String resourceVersion = v1Pod.getMetadata().getResourceVersion();

        DevopsEnvPodE devopsEnvPodE = new DevopsEnvPodE();
        devopsEnvPodE.setName(v1Pod.getMetadata().getName());
        devopsEnvPodE.setIp(v1Pod.getStatus().getPodIP());
        devopsEnvPodE.setStatus(status);
        devopsEnvPodE.setResourceVersion(resourceVersion);
        devopsEnvPodE.setNamespace(v1Pod.getMetadata().getNamespace());
        devopsEnvPodE.setReady(getReadyValue(status, v1Pod));
        devopsEnvPodE.setNodeName(v1Pod.getSpec().getNodeName());
        devopsEnvPodE.setRestartCount(K8sUtil.getRestartCountForPod(v1Pod));

        Boolean flag = false;
        if (applicationInstanceE.getId() != null) {
            List<DevopsEnvPodE> devopsEnvPodEList = devopsEnvPodRepository
                    .selectByInstanceId(applicationInstanceE.getId());
            handleEnvPod(v1Pod, applicationInstanceE, resourceVersion, devopsEnvPodE, flag, devopsEnvPodEList);
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


    private void handleEnvPod(V1Pod v1Pod, ApplicationInstanceE applicationInstanceE, String resourceVersion, DevopsEnvPodE devopsEnvPodE, Boolean flag, List<DevopsEnvPodE> devopsEnvPodEList) {
        if (devopsEnvPodEList == null || devopsEnvPodEList.isEmpty()) {
            devopsEnvPodE.initApplicationInstanceE(applicationInstanceE.getId());
            devopsEnvPodRepository.insert(devopsEnvPodE);
        } else {
            for (DevopsEnvPodE pod : devopsEnvPodEList) {
                if (pod.getName().equals(v1Pod.getMetadata().getName())
                        && pod.getNamespace().equals(v1Pod.getMetadata().getNamespace())) {
                    if (!resourceVersion.equals(pod.getResourceVersion())) {
                        devopsEnvPodE.setId(pod.getId());
                        devopsEnvPodE.initApplicationInstanceE(pod.getApplicationInstanceE().getId());
                        devopsEnvPodE.setObjectVersionNumber(pod.getObjectVersionNumber());
                        devopsEnvPodRepository.update(devopsEnvPodE);
                    }
                    flag = true;
                }
            }
            if (!flag) {
                devopsEnvPodE.initApplicationInstanceE(applicationInstanceE.getId());
                devopsEnvPodRepository.insert(devopsEnvPodE);
            }
        }
    }

    @Override
    public void handlerReleaseInstall(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }
        ReleasePayload releasePayload = JSONArray.parseObject(msg, ReleasePayload.class);
        List<Resource> resources = JSONArray.parseArray(releasePayload.getResources(), Resource.class);
        String releaseName = releasePayload.getName();
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(releaseName, envId);
        if (applicationInstanceE != null) {
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .query(applicationInstanceE.getCommandId());
            if (devopsEnvCommandE != null) {
                devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsEnvCommandRepository.update(devopsEnvCommandE);
                ApplicationVersionE applicationVersionE = applicationVersionRepository.queryByAppAndVersion(applicationInstanceE.getApplicationE().getId(), releasePayload.getChartVersion());
                applicationInstanceE.initApplicationVersionEById(applicationVersionE.getId());
                applicationInstanceE.setStatus(InstanceStatus.RUNNING.getStatus());
                applicationInstanceRepository.update(applicationInstanceE);
                installResource(resources, applicationInstanceE);
            }
        }
    }


    @Override
    public void handlerPreInstall(String key, String msg, Long clusterId) {
        if (msg.equals("null")) {
            return;
        }
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }

        List<Job> jobs = JSONArray.parseArray(msg, Job.class);
        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE();
        try {
            for (Job job : jobs) {
                applicationInstanceE = applicationInstanceRepository
                        .selectByCode(job.getReleaseName(), envId);
                DevopsEnvResourceE newdevopsEnvResourceE =
                        devopsEnvResourceRepository.queryResource(
                                applicationInstanceE.getId(),
                                applicationInstanceE.getCommandId(),
                                envId,
                                job.getKind(),
                                job.getName());
                DevopsEnvResourceE devopsEnvResourceE =
                        new DevopsEnvResourceE();
                devopsEnvResourceE.setKind(job.getKind());
                devopsEnvResourceE.setName(job.getName());
                devopsEnvResourceE.initDevopsEnvironmentE(envId);
                devopsEnvResourceE.setWeight(
                        TypeUtil.objToLong(job.getWeight()));
                DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
                devopsEnvResourceDetailE.setMessage(
                        FileUtil.yamlStringtoJson(job.getManifest()));
                saveOrUpdateResource(
                        devopsEnvResourceE,
                        newdevopsEnvResourceE,
                        devopsEnvResourceDetailE,
                        applicationInstanceE);
            }
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .query(applicationInstanceE.getCommandId());
            devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
            devopsEnvCommandRepository.update(devopsEnvCommandE);
        } catch (Exception e) {
            throw new CommonException("error.resource.insert", e);
        }
    }

    @Override
    public void resourceUpdate(String key, String msg, Long clusterId) {
        try {
            Long envId = getEnvId(key, clusterId);
            if (envId == null) {
                logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key) + "clusterId: " + clusterId);
                return;
            }

            Object obj = objectMapper.readValue(msg, Object.class);
            DevopsEnvResourceE devopsEnvResourceE = new DevopsEnvResourceE();
            DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
            devopsEnvResourceDetailE.setMessage(msg);
            devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
            devopsEnvResourceE.initDevopsEnvironmentE(envId);
            devopsEnvResourceE.setName(KeyParseTool.getResourceName(key));
            devopsEnvResourceE.setReversion(
                    TypeUtil.objToLong(
                            ((LinkedHashMap) ((LinkedHashMap) obj).get(METADATA)).get(RESOURCE_VERSION).toString()));
            String releaseName = null;
            DevopsEnvResourceE newdevopsEnvResourceE = null;
            ApplicationInstanceE applicationInstanceE = null;
            ResourceType resourceType = ResourceType.forString(KeyParseTool.getResourceType(key));
            if (resourceType == null) {
                resourceType = ResourceType.forString("MissType");
            }
            switch (resourceType) {
                case INGRESS:
                    newdevopsEnvResourceE =
                            devopsEnvResourceRepository.queryResource(
                                    null,
                                    null,
                                    envId,
                                    KeyParseTool.getResourceType(key),
                                    KeyParseTool.getResourceName(key));
                    //升级0.11.0-0.12.0,资源表新增envId,修复以前的域名数据
                    if (newdevopsEnvResourceE == null) {
                        newdevopsEnvResourceE = devopsEnvResourceRepository.queryResource(
                                null,
                                null,
                                null,
                                KeyParseTool.getResourceType(key),
                                KeyParseTool.getResourceName(key));
                    }
                    saveOrUpdateResource(devopsEnvResourceE, newdevopsEnvResourceE,
                            devopsEnvResourceDetailE, null);
                    break;
                case POD:
                    handlerUpdatePodMessage(key, msg, envId);
                    break;
                case SERVICE:
                    handleUpdateServiceMsg(key, envId, msg, devopsEnvResourceE);
                    break;
                case CONFIGMAP:
                    newdevopsEnvResourceE =
                            devopsEnvResourceRepository.queryResource(
                                    null,
                                    null,
                                    envId,
                                    KeyParseTool.getResourceType(key),
                                    KeyParseTool.getResourceName(key));
                    saveOrUpdateResource(devopsEnvResourceE, newdevopsEnvResourceE,
                            devopsEnvResourceDetailE, null);
                    break;
                case SECRET:
                    newdevopsEnvResourceE = devopsEnvResourceRepository
                            .queryResource(null, null, envId, KeyParseTool.getResourceType(key),
                                    KeyParseTool.getResourceName(key));
                    saveOrUpdateResource(devopsEnvResourceE, newdevopsEnvResourceE, devopsEnvResourceDetailE, null);
                    break;
                default:
                    releaseName = KeyParseTool.getReleaseName(key);
                    applicationInstanceE = applicationInstanceRepository.selectByCode(releaseName, envId);
                    if (applicationInstanceE == null) {
                        return;
                    }
                    newdevopsEnvResourceE =
                            devopsEnvResourceRepository.queryResource(
                                    applicationInstanceE.getId(),
                                    resourceType.getType().equals(ResourceType.JOB.getType()) ? applicationInstanceE.getCommandId() : null,
                                    envId,
                                    KeyParseTool.getResourceType(key),
                                    KeyParseTool.getResourceName(key));
                    if (newdevopsEnvResourceE == null) {
                        newdevopsEnvResourceE =
                                devopsEnvResourceRepository.queryResource(
                                        applicationInstanceE.getId(),
                                        resourceType.getType().equals(ResourceType.JOB.getType()) ? applicationInstanceE.getCommandId() : null,
                                        null,
                                        KeyParseTool.getResourceType(key),
                                        KeyParseTool.getResourceName(key));
                    }
                    saveOrUpdateResource(devopsEnvResourceE, newdevopsEnvResourceE, devopsEnvResourceDetailE, applicationInstanceE);
                    break;
            }
        } catch (IOException e) {
            logger.info(e.toString());
        }
    }

    private void handleUpdateServiceMsg(String key, Long envId, String msg, DevopsEnvResourceE devopsEnvResourceE) {
        ApplicationInstanceE applicationInstanceE;
        V1Service v1Service = json.deserialize(msg, V1Service.class);
        if (v1Service.getMetadata().getAnnotations() != null) {
            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
            if (devopsServiceE.getType().equals("LoadBalancer") &&
                    v1Service.getStatus() != null &&
                    v1Service.getStatus().getLoadBalancer() != null &&
                    !v1Service.getStatus().getLoadBalancer().getIngress().isEmpty()) {

                devopsServiceE.setLoadBalanceIp(v1Service.getStatus().getLoadBalancer().getIngress().get(0).getIp());
                List<PortMapE> portMapES = getPortMapES(v1Service);
                devopsServiceE.setPorts(portMapES);
                devopsServiceRepository.update(devopsServiceE);
            }
            if (devopsServiceE.getType().equals("NodePort") && v1Service.getSpec().getPorts() != null) {
                List<PortMapE> portMapES = getPortMapES(v1Service);
                devopsServiceE.setPorts(portMapES);
                devopsServiceRepository.update(devopsServiceE);

            }

            String releaseNames = v1Service.getMetadata().getAnnotations()
                    .get(CHOERODON_IO_NETWORK_SERVICE_INSTANCES);
            if (releaseNames != null) {
                String[] releases = releaseNames.split("\\+");
                List<Long> beforeInstanceIdS = devopsEnvResourceRepository.listByEnvAndType(envId, SERVICE_KIND).stream().filter(devopsEnvResourceE1 -> devopsEnvResourceE1.getName().equals(v1Service.getMetadata().getName())).map(devopsEnvResourceE1 ->
                        devopsEnvResourceE1.getApplicationInstanceE().getId()
                ).collect(Collectors.toList());
                List<Long> afterInstanceIds = new ArrayList<>();
                for (String release : releases) {
                    applicationInstanceE = applicationInstanceRepository
                            .selectByCode(release, envId);
                    if (applicationInstanceE != null) {
                        DevopsEnvResourceE newdevopsInsResourceE =
                                devopsEnvResourceRepository.queryResource(
                                        applicationInstanceE.getId(),
                                        null,
                                        null,
                                        KeyParseTool.getResourceType(key),
                                        KeyParseTool.getResourceName(key));
                        DevopsEnvResourceDetailE newDevopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
                        newDevopsEnvResourceDetailE.setMessage(msg);
                        saveOrUpdateResource(devopsEnvResourceE, newdevopsInsResourceE,
                                newDevopsEnvResourceDetailE, applicationInstanceE);
                        afterInstanceIds.add(applicationInstanceE.getId());
                    }
                }
                //网络更新实例删除网络以前实例网络关联的resource
                for (Long instanceId : beforeInstanceIdS) {
                    if (!afterInstanceIds.contains(instanceId)) {
                        devopsEnvResourceRepository.deleteByKindAndNameAndInstanceId(SERVICE_KIND, v1Service.getMetadata().getName(), instanceId);
                    }
                }
            }
        }
    }

    private List<PortMapE> getPortMapES(V1Service v1Service) {
        return v1Service.getSpec().getPorts().stream().map(v1ServicePort -> {
            PortMapE portMapE = new PortMapE();
            portMapE.setPort(TypeUtil.objToLong(v1ServicePort.getPort()));
            portMapE.setTargetPort(TypeUtil.objToString(v1ServicePort.getTargetPort()));
            portMapE.setNodePort(TypeUtil.objToLong(v1ServicePort.getNodePort()));
            portMapE.setProtocol(v1ServicePort.getProtocol());
            portMapE.setName(v1ServicePort.getName());
            return portMapE;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resourceDelete(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }
        if (KeyParseTool.getResourceType(key).equals(ResourceType.JOB.getType())) {
            return;
        }
        if (KeyParseTool.getResourceType(key).equals(ResourceType.POD.getType())) {
            String podName = KeyParseTool.getResourceName(key);
            String podNameSpace = KeyParseTool.getNamespace(key);
            devopsEnvPodRepository.deleteByName(podName, podNameSpace);
        }

        devopsEnvResourceRepository.deleteByEnvIdAndKindAndName(
                envId,
                KeyParseTool.getResourceType(key),
                KeyParseTool.getResourceName(key));

    }

    @Override
    public void helmReleaseHookLogs(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                .selectByCode(KeyParseTool.getReleaseName(key), envId);
        if (applicationInstanceE != null) {
            // 删除实例历史日志记录
            devopsEnvCommandLogRepository.deletePreInstanceCommandLog(applicationInstanceE.getId());
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INSTANCE.getType(), applicationInstanceE.getId());
            if (devopsEnvCommandE != null) {
                DevopsEnvCommandLogE devopsEnvCommandLogE = new DevopsEnvCommandLogE();
                devopsEnvCommandLogE.initDevopsEnvCommandE(devopsEnvCommandE.getId());
                devopsEnvCommandLogE.setLog(msg);
                devopsEnvCommandLogRepository.create(devopsEnvCommandLogE);
            }
        }
    }

    @Override
    public void updateInstanceStatus(String key, String releaseName, Long clusterId, String instanceStatus, String commandStatus, String msg) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectByCode(releaseName, envId);
        if (instanceE != null) {
            instanceE.setStatus(instanceStatus);
            applicationInstanceRepository.update(instanceE);
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INSTANCE.getType(), instanceE.getId());
            devopsEnvCommandE.setStatus(commandStatus);
            devopsEnvCommandE.setError(msg);
            devopsEnvCommandRepository.update(devopsEnvCommandE);
        }
    }

    @Override
    public void handlerDomainCreateMessage(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }

        V1beta1Ingress ingress = json.deserialize(msg, V1beta1Ingress.class);
        DevopsEnvResourceE devopsEnvResourceE = new DevopsEnvResourceE();
        DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
        devopsEnvResourceDetailE.setMessage(msg);
        devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
        devopsEnvResourceE.setName(KeyParseTool.getResourceName(key));
        devopsEnvResourceE.initDevopsEnvironmentE(envId);
        devopsEnvResourceE.setReversion(TypeUtil.objToLong(ingress.getMetadata().getResourceVersion()));
        DevopsEnvResourceE newDevopsEnvResourceE =
                devopsEnvResourceRepository.queryResource(
                        null,
                        null,
                        envId,
                        KeyParseTool.getResourceType(key),
                        KeyParseTool.getResourceName(key));
        if (newDevopsEnvResourceE == null) {
            newDevopsEnvResourceE =
                    devopsEnvResourceRepository.queryResource(
                            null,
                            null,
                            null,
                            KeyParseTool.getResourceType(key),
                            KeyParseTool.getResourceName(key));
        }
        saveOrUpdateResource(devopsEnvResourceE, newDevopsEnvResourceE, devopsEnvResourceDetailE, null);
        String ingressName = ingress.getMetadata().getName();
        devopsIngressRepository.setStatus(envId, ingressName, IngressStatus.RUNNING.getStatus());

    }

    @Override
    public void helmReleasePreUpgrade(String key, String msg, Long clusterId) {
        handlerPreInstall(key, msg, clusterId);
    }

    @Override
    public void handlerReleaseUpgrade(String key, String msg, Long clusterId) {
        handlerReleaseInstall(key, msg, clusterId);
    }


    @Override
    public void helmReleaseDeleteFail(String key, String msg, Long clusterId) {

        updateInstanceStatus(key, KeyParseTool.getReleaseName(key),
                clusterId,
                InstanceStatus.DELETED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaseStartFail(String key, String msg, Long clusterId) {
        updateInstanceStatus(
                key,
                KeyParseTool.getReleaseName(key),
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
                key, KeyParseTool.getReleaseName(key),
                clusterId,
                InstanceStatus.FAILED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaseUpgradeFail(String key, String msg, Long clusterId) {

        updateInstanceStatus(key, KeyParseTool.getReleaseName(key),
                clusterId,
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaeStopFail(String key, String msg, Long clusterId) {

        updateInstanceStatus(key, KeyParseTool.getReleaseName(key),
                clusterId,
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);

    }

    @Override
    public void commandNotSend(Long commandId, String msg) {
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(commandId);
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getStatus());
        devopsEnvCommandE.setError(msg);
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        if (devopsEnvCommandE.getCommandType().equals(CommandType.CREATE.getType())) {
            if (devopsEnvCommandE.getObject().equals(ObjectType.INSTANCE.getType())) {
                ApplicationInstanceE applicationInstanceE =
                        applicationInstanceRepository.selectById(devopsEnvCommandE.getObjectId());
                applicationInstanceE.setStatus(InstanceStatus.FAILED.getStatus());
                applicationInstanceRepository.update(applicationInstanceE);
            } else if (devopsEnvCommandE.getObject().equals(ObjectType.SERVICE.getType())) {
                DevopsServiceE devopsServiceE = devopsServiceRepository.query(devopsEnvCommandE.getObjectId());
                devopsServiceE.setStatus(ServiceStatus.FAILED.getStatus());
                devopsServiceRepository.update(devopsServiceE);
            } else if (devopsEnvCommandE.getObject().equals(ObjectType.INGRESS.getType())) {
                DevopsIngressDO ingress = devopsIngressRepository.getIngress(devopsEnvCommandE.getObjectId());
                ingress.setStatus(IngressStatus.FAILED.getStatus());
                devopsIngressRepository.updateIngress(ingress);
            }
        }
    }

    @Override
    public void resourceSync(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }

        ResourceSyncPayload resourceSyncPayload = JSONArray.parseObject(msg, ResourceSyncPayload.class);
        ResourceType resourceType = ResourceType.forString(resourceSyncPayload.getResourceType());
        List<DevopsEnvResourceE> devopsEnvResourceES;
        if (resourceType == null) {
            resourceType = ResourceType.forString("MissType");
        }
        if (resourceSyncPayload.getResources() == null) {
            return;
        }
        switch (resourceType) {
            case POD:
                devopsEnvResourceES = devopsEnvResourceRepository
                        .listByEnvAndType(envId, ResourceType.POD.getType());
                if (!devopsEnvResourceES.isEmpty()) {
                    List<String> podNames = Arrays.asList(resourceSyncPayload.getResources());
                    devopsEnvResourceES.stream()
                            .filter(devopsEnvResourceE -> !podNames.contains(devopsEnvResourceE.getName()))
                            .forEach(devopsEnvResourceE -> {
                                devopsEnvResourceRepository.deleteByEnvIdAndKindAndName(
                                        envId, ResourceType.POD.getType(), devopsEnvResourceE.getName());
                                devopsEnvPodRepository.deleteByName(
                                        devopsEnvResourceE.getName(), KeyParseTool.getValue(key, "env"));
                            });
                }
                break;
            case DEPLOYMENT:
                devopsEnvResourceES = devopsEnvResourceRepository
                        .listByEnvAndType(envId, ResourceType.DEPLOYMENT.getType());
                if (!devopsEnvResourceES.isEmpty()) {
                    List<String> deploymentNames = Arrays.asList(resourceSyncPayload.getResources());
                    devopsEnvResourceES.stream()
                            .filter(devopsEnvResourceE -> !deploymentNames.contains(devopsEnvResourceE.getName()))
                            .forEach(devopsEnvResourceE ->
                                    devopsEnvResourceRepository.deleteByEnvIdAndKindAndName(
                                            envId, ResourceType.DEPLOYMENT.getType(), devopsEnvResourceE.getName()));
                }
                break;
            case REPLICASET:
                devopsEnvResourceES = devopsEnvResourceRepository
                        .listByEnvAndType(envId, ResourceType.REPLICASET.getType());
                if (!devopsEnvResourceES.isEmpty()) {
                    List<String> replicaSetNames = Arrays.asList(resourceSyncPayload.getResources());
                    devopsEnvResourceES.stream()
                            .filter(devopsEnvResourceE -> !replicaSetNames.contains(devopsEnvResourceE.getName()))
                            .forEach(devopsEnvResourceE ->
                                    devopsEnvResourceRepository.deleteByEnvIdAndKindAndName(
                                            envId, ResourceType.REPLICASET.getType(), devopsEnvResourceE.getName()));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void jobEvent(String msg) {
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
    public void releasePodEvent(String msg) {
        Event event = JSONArray.parseObject(msg, Event.class);
        insertDevopsCommandEvent(event, ResourceType.POD.getType());
    }

    @Override
    public void gitOpsSyncEvent(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }

        logger.info("env {} receive git ops msg :\n{}", envId, msg);
        GitOpsSync gitOpsSync = JSONArray.parseObject(msg, GitOpsSync.class);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        DevopsEnvCommitE agentSyncCommit = devopsEnvCommitRepository.query(devopsEnvironmentE.getAgentSyncCommit());
        if (agentSyncCommit != null && agentSyncCommit.getCommitSha().equals(gitOpsSync.getMetadata().getCommit())) {
            return;
        }
        DevopsEnvCommitE devopsEnvCommitE = devopsEnvCommitRepository.queryByEnvIdAndCommit(envId, gitOpsSync.getMetadata().getCommit());
        if (devopsEnvCommitE == null) {
            return;
        }
        devopsEnvironmentE.setAgentSyncCommit(devopsEnvCommitE.getId());
        devopsEnvironmentRepository.updateAgentSyncEnvCommit(devopsEnvironmentE);
        if (gitOpsSync.getResourceIDs() == null) {
            return;
        }
        if (gitOpsSync.getResourceIDs().isEmpty()) {
            return;
        }
        List<DevopsEnvFileErrorE> errorDevopsFiles = getEnvFileErrors(envId, gitOpsSync, devopsEnvironmentE);

        gitOpsSync.getMetadata().getFilesCommit().forEach(fileCommit -> {
            DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository.queryByEnvAndPath(devopsEnvironmentE.getId(), fileCommit.getFile());
            devopsEnvFileE.setAgentCommit(fileCommit.getCommit());
            devopsEnvFileRepository.update(devopsEnvFileE);
        });
        gitOpsSync.getMetadata().getResourceCommits()
                .forEach(resourceCommit -> {
                    String[] objects = resourceCommit.getResourceId().split("/");
                    switch (objects[0]) {
                        case C7NHELMRELEASE_KIND:
                            syncC7nHelmRelease(envId, errorDevopsFiles, resourceCommit, objects);
                            break;
                        case INGRESS_KIND:
                            syncIngress(envId, errorDevopsFiles, resourceCommit, objects);
                            break;
                        case SERVICE_KIND:
                            syncService(envId, errorDevopsFiles, resourceCommit, objects);
                            break;
                        case CERTIFICATE_KIND:
                            syncCetificate(envId, errorDevopsFiles, resourceCommit, objects);
                            break;
                        case CONFIGMAP_KIND:
                            syncConfigMap(envId, errorDevopsFiles, resourceCommit, objects);
                            break;
                        case SECRET_KIND:
                            syncSecret(envId, errorDevopsFiles, resourceCommit, objects);
                            break;
                        default:
                            break;
                    }
                });
    }

    private void syncSecret(Long envId, List<DevopsEnvFileErrorE> envFileErrorFiles, ResourceCommit resourceCommit,
                            String[] objects) {
        DevopsEnvFileResourceE devopsEnvFileResourceE;
        DevopsSecretE devopsSecretE = devopsSecretRepository.selectByEnvIdAndName(envId, objects[1]);
        devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(envId, devopsSecretE.getId(), ObjectType.SECRET.getType());
        if (updateEnvCommandStatus(resourceCommit, devopsSecretE.getCommandId(), devopsEnvFileResourceE,
                SECRET_KIND, devopsSecretE.getName(), CommandStatus.SUCCESS.getStatus(), envFileErrorFiles)) {
            devopsSecretE.setStatus(SecretStatus.FAILED.getStatus());
        } else {
            devopsSecretE.setStatus(SecretStatus.SUCCESS.getStatus());
        }
        devopsSecretRepository.update(devopsSecretE);
    }

    private void syncCetificate(Long envId, List<DevopsEnvFileErrorE> errorDevopsFiles, ResourceCommit resourceCommit, String[] objects) {
        DevopsEnvFileResourceE devopsEnvFileResourceE;
        CertificationE certificationE = certificationRepository
                .queryByEnvAndName(envId, objects[1]);
        devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(
                        envId, certificationE.getId(), "Certificate");
        if (updateEnvCommandStatus(resourceCommit, certificationE.getCommandId(),
                devopsEnvFileResourceE, CERTIFICATE_KIND, certificationE.getName(),
                null, errorDevopsFiles)) {
            certificationE.setStatus(CertificationStatus.FAILED.getStatus());
        } else {
            certificationE.setStatus(CertificationStatus.APPLYING.getStatus());
        }
        certificationRepository.updateStatus(certificationE);
    }

    private void syncService(Long envId, List<DevopsEnvFileErrorE> errorDevopsFiles, ResourceCommit resourceCommit, String[] objects) {
        DevopsEnvFileResourceE devopsEnvFileResourceE;
        DevopsServiceE devopsServiceE = devopsServiceRepository
                .selectByNameAndEnvId(objects[1], envId);
        devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(envId, devopsServiceE.getId(), "Service");
        if (updateEnvCommandStatus(resourceCommit, devopsServiceE.getCommandId(),
                devopsEnvFileResourceE, SERVICE_KIND, devopsServiceE.getName(), CommandStatus.SUCCESS.getStatus(), errorDevopsFiles)) {
            devopsServiceE.setStatus(ServiceStatus.FAILED.getStatus());
        } else {
            devopsServiceE.setStatus(ServiceStatus.RUNNING.getStatus());
        }
        devopsServiceRepository.update(devopsServiceE);
    }

    private void syncIngress(Long envId, List<DevopsEnvFileErrorE> errorDevopsFiles, ResourceCommit resourceCommit, String[] objects) {
        DevopsEnvFileResourceE devopsEnvFileResourceE;
        DevopsIngressE devopsIngressE = devopsIngressRepository
                .selectByEnvAndName(envId, objects[1]);
        devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(envId, devopsIngressE.getId(), "Ingress");
        if (updateEnvCommandStatus(resourceCommit, devopsIngressE.getCommandId(),
                devopsEnvFileResourceE, INGRESS_KIND, devopsIngressE.getName(), CommandStatus.SUCCESS.getStatus(), errorDevopsFiles)) {
            devopsIngressRepository.setStatus(envId, devopsIngressE.getName(), IngressStatus.FAILED.getStatus());
        } else {
            devopsIngressRepository.setStatus(envId, devopsIngressE.getName(), IngressStatus.RUNNING.getStatus());
        }
    }

    private void syncC7nHelmRelease(Long envId, List<DevopsEnvFileErrorE> errorDevopsFiles, ResourceCommit resourceCommit, String[] objects) {
        DevopsEnvFileResourceE devopsEnvFileResourceE;
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                .selectByCode(objects[1], envId);
        devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(envId, applicationInstanceE.getId(), "C7NHelmRelease");
        if (updateEnvCommandStatus(resourceCommit, applicationInstanceE.getCommandId(),
                devopsEnvFileResourceE, C7NHELMRELEASE_KIND, applicationInstanceE.getCode(), null, errorDevopsFiles)) {
            applicationInstanceE.setStatus(InstanceStatus.FAILED.getStatus());
            applicationInstanceRepository.update(applicationInstanceE);
        }
    }

    private void syncConfigMap(Long envId, List<DevopsEnvFileErrorE> errorDevopsFiles, ResourceCommit resourceCommit, String[] objects) {
        DevopsEnvFileResourceE devopsEnvFileResourceE;
        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository
                .queryByEnvIdAndName(envId, objects[1]);
        devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(envId, devopsConfigMapE.getId(), "ConfigMap");
        updateEnvCommandStatus(resourceCommit, devopsConfigMapE.getDevopsEnvCommandE().getId(),
                devopsEnvFileResourceE, CONFIGMAP_KIND, devopsConfigMapE.getName(), CommandStatus.SUCCESS.getStatus(), errorDevopsFiles);
    }

    private List<DevopsEnvFileErrorE> getEnvFileErrors(Long envId, GitOpsSync gitOpsSync, DevopsEnvironmentE devopsEnvironmentE) {
        List<DevopsEnvFileErrorE> errorDevopsFiles = new ArrayList<>();
        if (gitOpsSync.getMetadata().getErrors() != null) {
            gitOpsSync.getMetadata().getErrors().stream().forEach(error -> {
                DevopsEnvFileErrorE devopsEnvFileErrorE = devopsEnvFileErrorRepository.queryByEnvIdAndFilePath(envId, error.getPath());
                if (devopsEnvFileErrorE == null) {
                    devopsEnvFileErrorE = new DevopsEnvFileErrorE();
                    devopsEnvFileErrorE.setCommit(error.getCommit());
                    devopsEnvFileErrorE.setError(error.getError());
                    devopsEnvFileErrorE.setFilePath(error.getPath());
                    devopsEnvFileErrorE.setEnvId(devopsEnvironmentE.getId());
                    devopsEnvFileErrorE = devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                    devopsEnvFileErrorE.setResource(error.getId());
                } else {
                    devopsEnvFileErrorE.setError(devopsEnvFileErrorE.getError() + error.getError());
                    devopsEnvFileErrorE = devopsEnvFileErrorRepository.createOrUpdate(devopsEnvFileErrorE);
                    devopsEnvFileErrorE.setResource(error.getId());
                }
                errorDevopsFiles.add(devopsEnvFileErrorE);
            });
        }
        return errorDevopsFiles;
    }

    private boolean updateEnvCommandStatus(ResourceCommit resourceCommit, Long commandId,
                                           DevopsEnvFileResourceE devopsEnvFileResourceE,
                                           String objectType, String objectName, String passStatus,
                                           List<DevopsEnvFileErrorE> envFileErrorES) {
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(commandId);
        if (resourceCommit.getCommit().equals(devopsEnvCommandE.getSha()) && passStatus != null) {
            devopsEnvCommandE.setStatus(passStatus);
        }
        DevopsEnvFileErrorE devopsEnvFileErrorE = devopsEnvFileErrorRepository
                .queryByEnvIdAndFilePath(devopsEnvFileResourceE.getEnvironment().getId(), devopsEnvFileResourceE.getFilePath());
        if (devopsEnvFileErrorE != null) {
            List<DevopsEnvFileErrorE> devopsEnvFileErrorES = envFileErrorES.stream().filter(devopsEnvFileErrorE1 -> devopsEnvFileErrorE1.getId().equals(devopsEnvFileErrorE.getId())).collect(Collectors.toList());
            if (!devopsEnvFileErrorES.isEmpty()) {
                String[] objects = devopsEnvFileErrorES.get(0).getResource().split("/");
                if (objects[0].equals(objectType) && objects[1].equals(objectName)) {
                    devopsEnvCommandE.setStatus(CommandStatus.FAILED.getStatus());
                    devopsEnvCommandE.setError(devopsEnvFileErrorE.getError());
                    devopsEnvCommandRepository.update(devopsEnvCommandE);
                    return true;
                }
            }
        }
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        return false;
    }

    private void saveOrUpdateResource(DevopsEnvResourceE devopsEnvResourceE,
                                      DevopsEnvResourceE newdevopsEnvResourceE,
                                      DevopsEnvResourceDetailE devopsEnvResourceDetailE,
                                      ApplicationInstanceE applicationInstanceE) {
        if (applicationInstanceE != null) {
            devopsEnvResourceE.initApplicationInstanceE(applicationInstanceE.getId());
            devopsEnvResourceE.initDevopsEnvCommandE(applicationInstanceE.getCommandId());
        }
        if (newdevopsEnvResourceE == null) {
            devopsEnvResourceE.initDevopsInstanceResourceMessageE(
                    devopsEnvResourceDetailRepository.create(devopsEnvResourceDetailE).getId());
            devopsEnvResourceRepository.create(devopsEnvResourceE);
            return;
        }
        if (newdevopsEnvResourceE.getReversion() == null) {
            newdevopsEnvResourceE.setReversion(0L);
        }
        if (devopsEnvResourceE.getReversion() == null) {
            devopsEnvResourceE.setReversion(0L);
        }
        if (applicationInstanceE != null) {
            newdevopsEnvResourceE.initDevopsEnvCommandE(applicationInstanceE.getCommandId());
            newdevopsEnvResourceE.initApplicationInstanceE(devopsEnvResourceE.getApplicationInstanceE().getId());
        }
        if (devopsEnvResourceE.getDevopsEnvironmentE() != null) {
            newdevopsEnvResourceE.initDevopsEnvironmentE(devopsEnvResourceE.getDevopsEnvironmentE().getId());

            devopsEnvResourceRepository.update(newdevopsEnvResourceE);
        }
        if (!newdevopsEnvResourceE.getReversion().equals(devopsEnvResourceE.getReversion())) {
            newdevopsEnvResourceE.setReversion(devopsEnvResourceE.getReversion());
            devopsEnvResourceDetailE.setId(
                    newdevopsEnvResourceE.getDevopsEnvResourceDetailE().getId());
            devopsEnvResourceRepository.update(newdevopsEnvResourceE);
            devopsEnvResourceDetailRepository.update(devopsEnvResourceDetailE);
        }

    }

    private void installResource(List<Resource> resources, ApplicationInstanceE applicationInstanceE) {
        try {
            for (Resource resource : resources) {
                Long instanceId = applicationInstanceE.getId();
                if (resource.getKind().equals(ResourceType.INGRESS.getType())) {
                    instanceId = null;
                }
                DevopsEnvResourceE newdevopsEnvResourceE =
                        devopsEnvResourceRepository.queryResource(
                                instanceId,
                                null, null,
                                resource.getKind(),
                                resource.getName());
                DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
                devopsEnvResourceDetailE.setMessage(resource.getObject());
                DevopsEnvResourceE devopsEnvResourceE =
                        new DevopsEnvResourceE();
                devopsEnvResourceE.setKind(resource.getKind());
                devopsEnvResourceE.setName(resource.getName());
                devopsEnvResourceE.initDevopsEnvironmentE(applicationInstanceE.getDevopsEnvironmentE().getId());
                JSONObject jsonResult = JSONObject.parseObject(JSONObject.parseObject(resource.getObject())
                        .get(METADATA).toString());
                devopsEnvResourceE.setReversion(
                        TypeUtil.objToLong(jsonResult.get(RESOURCE_VERSION).toString()));
                saveOrUpdateResource(
                        devopsEnvResourceE,
                        newdevopsEnvResourceE,
                        devopsEnvResourceDetailE,
                        applicationInstanceE);
                if (resource.getKind().equals(ResourceType.POD.getType())) {
                    syncPod(resource.getObject(), applicationInstanceE);
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }


    private void syncService(DevopsServiceE devopsServiceE, String msg, ApplicationInstanceE applicationInstanceE) {
        V1Service v1Service = json.deserialize(msg, V1Service.class);
        Map<String, String> lab = v1Service.getMetadata().getLabels();
        if (lab.get(SERVICE_LABLE) != null && lab.get(SERVICE_LABLE).equals(SERVICE_KIND)) {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(
                    applicationInstanceE.getDevopsEnvironmentE().getId());
            if (devopsServiceRepository.selectByNameAndEnvId(
                    v1Service.getMetadata().getName(), devopsEnvironmentE.getId()) == null) {
                devopsServiceE.setEnvId(devopsEnvironmentE.getId());
                devopsServiceE.setAppId(applicationInstanceE.getApplicationE().getId());
                devopsServiceE.setName(v1Service.getMetadata().getName());
                devopsServiceE.setType(v1Service.getSpec().getType());
                devopsServiceE.setStatus(ServiceStatus.RUNNING.getStatus());
                devopsServiceE.setPorts(gson.fromJson(
                        gson.toJson(v1Service.getSpec().getPorts()),
                        new TypeToken<ArrayList<PortMapE>>() {
                        }.getType()));
                if (v1Service.getSpec().getExternalIPs() != null) {
                    devopsServiceE.setExternalIp(String.join(",", v1Service.getSpec().getExternalIPs()));
                }
                devopsServiceE.setLabels(json.serialize(v1Service.getMetadata().getLabels()));
                devopsServiceE.setAnnotations(json.serialize(v1Service.getMetadata().getAnnotations()));
                devopsServiceE.setId(devopsServiceRepository.insert(devopsServiceE).getId());

                DevopsServiceAppInstanceE devopsServiceAppInstanceE = devopsServiceInstanceRepository
                        .queryByOptions(devopsServiceE.getId(), applicationInstanceE.getId());
                if (devopsServiceAppInstanceE == null) {
                    devopsServiceAppInstanceE = new DevopsServiceAppInstanceE();
                    devopsServiceAppInstanceE.setServiceId(devopsServiceE.getId());
                    devopsServiceAppInstanceE.setAppInstanceId(applicationInstanceE.getId());
                    devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
                }

                DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
                devopsEnvCommandE.setObject(ObjectType.SERVICE.getType());
                devopsEnvCommandE.setObjectId(devopsServiceE.getId());
                devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
                devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsEnvCommandRepository.create(devopsEnvCommandE);
            }
        } else {
            devopsEnvResourceRepository.deleteByEnvIdAndKindAndName(applicationInstanceE.getDevopsEnvironmentE().getId(),
                    ResourceType.SERVICE.getType(), v1Service.getMetadata().getName());
        }
    }


    private void syncPod(String msg, ApplicationInstanceE applicationInstanceE) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);
        String status = K8sUtil.changePodStatus(v1Pod);
        String resourceVersion = v1Pod.getMetadata().getResourceVersion();

        DevopsEnvPodE devopsEnvPodE = new DevopsEnvPodE();
        devopsEnvPodE.setName(v1Pod.getMetadata().getName());
        devopsEnvPodE.setIp(v1Pod.getStatus().getPodIP());
        devopsEnvPodE.setStatus(status);
        devopsEnvPodE.setResourceVersion(resourceVersion);
        devopsEnvPodE.setNamespace(v1Pod.getMetadata().getNamespace());
        devopsEnvPodE.setReady(getReadyValue(status, v1Pod));
        devopsEnvPodE.initApplicationInstanceE(applicationInstanceE.getId());
        devopsEnvPodE.setNodeName(v1Pod.getSpec().getNodeName());
        devopsEnvPodE.setRestartCount(K8sUtil.getRestartCountForPod(v1Pod));
        devopsEnvPodRepository.insert(devopsEnvPodE);
        Long podId = devopsEnvPodRepository.get(devopsEnvPodE).getId();
    }

    private void insertDevopsCommandEvent(Event event, String type) {
        DevopsEnvResourceE devopsEnvResourceE = devopsEnvResourceRepository
                .queryLatestJob(event.getInvolvedObject().getKind(), event.getInvolvedObject().getName());
        try {
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INSTANCE.getType(), devopsEnvResourceE.getApplicationInstanceE().getId());
            // 删除实例事件记录
            devopsCommandEventRepository.deletePreInstanceCommandEvent(devopsEnvCommandE.getObjectId());
            DevopsCommandEventE devopsCommandEventE = new DevopsCommandEventE();
            devopsCommandEventE.setEventCreationTime(event.getMetadata().getCreationTimestamp());
            devopsCommandEventE.setMessage(event.getMessage());
            devopsCommandEventE.setName(event.getInvolvedObject().getName());
            devopsCommandEventE.initDevopsEnvCommandE(devopsEnvCommandE.getId());
            devopsCommandEventE.setType(type);
            devopsCommandEventRepository.create(devopsCommandEventE);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }


    @Override
    public List<ApplicationE> getApplication(String appName, Long projectId, Long orgId) {
        List<ApplicationE> applications = new ArrayList<>();
        ApplicationE applicationE = applicationRepository
                .queryByCode(appName, projectId);
        if (applicationE != null) {
            applications.add(applicationE);
        }
        List<ApplicationE> applicationES = applicationRepository.listByCode(appName);
        List<ApplicationE> applicationList = applicationES.stream()
                .filter(newApplicationE ->
                        iamRepository.queryIamProject(newApplicationE.getProjectE().getId())
                                .getOrganization().getId().equals(orgId))
                .collect(Collectors.toList());
        applications.addAll(findAppInAppMarket(applicationES, applicationList));
        return applications;
    }

    private List<ApplicationE> findAppInAppMarket(List<ApplicationE> applicationES, List<ApplicationE> applicationList) {
        List<ApplicationE> applications = new ArrayList<>();
        if (!applicationList.isEmpty()) {
            applicationList.forEach(applicationE -> {
                if (applicationMarketMapper.selectCountByAppId(applicationE.getId()) != 0) {
                    applications.add(applicationE);
                }
            });
        }
        if (!applicationES.isEmpty()) {
            applicationES.forEach(applicationE -> {
                ApplicationMarketE applicationMarketE =
                        applicationMarketRepository.queryByAppId(applicationE.getId());
                if (applicationMarketE != null
                        || !applicationMarketE.getPublishLevel().equals(PUBLIC)) {
                    applications.add(applicationE);
                }
            });
        }
        return applications;
    }

    @Override
    public void gitOpsCommandSyncEvent(String key, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }

        logger.info("sync command status!");
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        List<Command> commands = new ArrayList<>();
        getCommands(envId, commands);
        Msg msg = new Msg();
        msg.setKey(String.format("cluster:%d.env:%s.envId:%d",
                clusterId,
                devopsEnvironmentE.getCode(),
                envId));
        msg.setType(HelmType.STATUS_SYNC.toValue());
        try {
            msg.setPayload(JSONArray.toJSONString(commands));
        } catch (Exception e) {
            throw new CommonException("error.payload.error", e);
        }
        socketMsgDispatcher.dispatcher(msg);

    }


    private void getCommands(Long envId, List<Command> commands) {
        List<Command> removeCommands = new ArrayList<>();
        applicationInstanceRepository.selectByEnvId(envId).stream().forEach(applicationInstanceE -> {
            Long commandId = applicationInstanceE.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(applicationInstanceE.getCommandId());
                command.setResourceType(INSTANCE_KIND);
                command.setResourceName(applicationInstanceE.getCode());
                commands.add(command);
            }
        });
        devopsServiceRepository.selectByEnvId(envId).stream().forEach(devopsServiceE -> {
            Long commandId = devopsServiceE.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(devopsServiceE.getCommandId());
                command.setResourceType(SERVICE_KIND);
                command.setResourceName(devopsServiceE.getName());
                commands.add(command);
            }
        });
        devopsIngressRepository.listByEnvId(envId).stream().forEach(devopsIngressE -> {
            Long commandId = devopsIngressE.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(devopsIngressE.getCommandId());
                command.setResourceType(INGRESS_KIND);
                command.setResourceName(devopsIngressE.getName());
                commands.add(command);
            }
        });
        certificationRepository.listByEnvId(envId).stream().forEach(certificationE -> {
            Long commandId = certificationE.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(certificationE.getCommandId());
                command.setResourceType(CERTIFICATE_KIND);
                command.setResourceName(certificationE.getName());
                commands.add(command);
            }
        });
        for (DevopsConfigMapE devopsConfigMapE : devopsConfigMapRepository.listByEnv(envId)) {
            Long commandId = devopsConfigMapE.getDevopsEnvCommandE().getId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(commandId);
                command.setResourceType(CONFIGMAP_KIND);
                command.setResourceName(devopsConfigMapE.getName());
                commands.add(command);
            }
        }
        devopsSecretRepository.listByEnv(envId).forEach(devopsSecretE -> {
            Long commandId = devopsSecretE.getCommandId();
            if (commandId != null) {
                Command command = new Command();
                command.setId(commandId);
                command.setResourceType(SECRET_KIND);
                command.setResourceName(devopsSecretE.getName());
                commands.add(command);
            }
        });
        Date d = new Date();
        if (!commands.isEmpty()) {
            commands.stream().forEach(command -> {
                DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(command.getId());
                command.setCommit(devopsEnvCommandE.getSha());
                if (!CommandStatus.OPERATING.getStatus().equals(devopsEnvCommandE.getStatus()) || (CommandStatus.OPERATING.getStatus().equals(devopsEnvCommandE.getStatus()) && d.getTime() - devopsEnvCommandE.getLastUpdateDate().getTime() <= 180000)) {
                    removeCommands.add(command);
                }
            });
            commands.removeAll(removeCommands);
        }
    }


    @Override
    public void gitOpsCommandSyncEventResult(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }

        logger.info("sync command status result: {}.", msg);
        List<Command> commands = JSONArray.parseArray(msg, Command.class);
        List<Command> oldCommands = new ArrayList<>();
        getCommands(envId, oldCommands);
        if (!oldCommands.isEmpty()) {
            oldCommands.stream().filter(oldComand -> oldComand.getId() != null).forEach(command ->
                    commands.stream().filter(command1 -> command1.getId() != null && command1.getId().equals(command.getId())).forEach(command1 -> {
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(command.getId());
                        if (command1.getCommit() != null && command.getCommit() != null && command.getCommit().equals(command1.getCommit())) {
                            devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
                            updateResourceStatus(envId, devopsEnvCommandE, InstanceStatus.RUNNING, ServiceStatus.RUNNING, IngressStatus.RUNNING, CertificationStatus.ACTIVE);
                        } else {
                            devopsEnvCommandE.setStatus(CommandStatus.FAILED.getStatus());
                            devopsEnvCommandE.setError("The deploy is time out!");
                            updateResourceStatus(envId, devopsEnvCommandE, InstanceStatus.FAILED, ServiceStatus.FAILED, IngressStatus.FAILED, CertificationStatus.FAILED);
                        }
                        devopsEnvCommandRepository.update(devopsEnvCommandE);
                    })
            );
        }
    }

    @Override
    public void handlerServiceCreateMessage(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }

        DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                KeyParseTool.getResourceName(key), envId);
        try {
            V1Service v1Service = json.deserialize(msg, V1Service.class);
            String releaseNames = v1Service.getMetadata().getAnnotations()
                    .get(CHOERODON_IO_NETWORK_SERVICE_INSTANCES);
            String[] releases = releaseNames.split("\\+");
            DevopsEnvResourceE devopsEnvResourceE =
                    DevopsInstanceResourceFactory.createDevopsInstanceResourceE();
            DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
            devopsEnvResourceDetailE.setMessage(msg);
            devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
            devopsEnvResourceE.setName(v1Service.getMetadata().getName());
            devopsEnvResourceE.initDevopsEnvironmentE(envId);
            devopsEnvResourceE.setReversion(TypeUtil.objToLong(v1Service.getMetadata().getResourceVersion()));
            for (String release : releases) {
                ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                        .selectByCode(release, envId);

                DevopsEnvResourceE newdevopsEnvResourceE = devopsEnvResourceRepository
                        .queryResource(
                                applicationInstanceE.getId(),
                                null, null,
                                KeyParseTool.getResourceType(key),
                                KeyParseTool.getResourceName(key));
                saveOrUpdateResource(devopsEnvResourceE,
                        newdevopsEnvResourceE,
                        devopsEnvResourceDetailE,
                        applicationInstanceE);
            }
            devopsServiceE.setStatus(ServiceStatus.RUNNING.getStatus());
            devopsServiceRepository.update(devopsServiceE);
            DevopsEnvCommandE newdevopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.SERVICE.getType(), devopsServiceE.getId());
            newdevopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
            devopsEnvCommandRepository.update(newdevopsEnvCommandE);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void updateNamespaces(String msg, Long clusterId) {
        DevopsClusterE devopsClusterE = devopsClusterRepository.query(clusterId);
        devopsClusterE.setNamespaces(msg);
        devopsClusterRepository.update(devopsClusterE);

    }

    @Override
    public void upgradeCluster(String key, String msg) {
        //0.10.0-0.11.0  初始化集群信息
        logger.info(String.format("upgradeCluster message: %s", msg));
        UpgradeCluster upgradeCluster = json.deserialize(msg, UpgradeCluster.class);
        DevopsClusterE devopsClusterE = devopsClusterRepository.queryByToken(upgradeCluster.getToken());
        if (devopsClusterE == null) {
            logger.info(String.format("the cluster is not exist: %s", upgradeCluster.getToken()));
            return;
        }
        if (devopsClusterE.getInit() != null) {
            logger.info(String.format("the cluster has bean init: %s", devopsClusterE.getName()));
            return;
        }
        if (upgradeCluster.getEnvs() != null) {
            upgradeCluster.getEnvs().forEach(clusterEnv -> {
                DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(clusterEnv.getEnvId());
                if (devopsEnvironmentE != null && devopsEnvironmentE.getCode().equals(clusterEnv.getNamespace())) {
                    ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
                    if (projectE.getOrganization().getId().equals(devopsClusterE.getOrganizationId())) {
                        DevopsClusterProPermissionE devopsClusterProPermissionE = new DevopsClusterProPermissionE();
                        devopsClusterProPermissionE.setProjectId(projectE.getId());
                        devopsClusterProPermissionE.setClusterId(devopsClusterE.getId());
                        devopsClusterProPermissionRepository.insert(devopsClusterProPermissionE);
                        devopsEnvironmentE.initDevopsClusterEById(devopsClusterE.getId());
                        devopsEnvironmentRepository.update(devopsEnvironmentE);
                    }
                }
            });
            devopsClusterE.setSkipCheckProjectPermission(false);
        }
        devopsClusterE.setInit(true);
        devopsClusterRepository.update(devopsClusterE);
        GitConfigDTO gitConfigDTO = gitUtil.getGitConfig(devopsClusterE.getId());
        Msg initClusterEnv = new Msg();
        try {
            initClusterEnv.setPayload(mapper.writeValueAsString(gitConfigDTO));
        } catch (IOException e) {
            throw new CommonException("read envId from agent session failed", e);
        }
        initClusterEnv.setType(INIT_AGENT);
        initClusterEnv.setKey(String.format("cluster:%s", devopsClusterE.getId()
        ));
        socketMsgDispatcher.dispatcher(initClusterEnv);
    }


    @Override
    @Saga(code = "test-pod-update-saga",
            description = "测试Pod升级(test pod update saga)", inputSchema = "{}")
    public void testPodUpdate(String key, String msg, Long clusterId) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);
        String status = K8sUtil.changePodStatus(v1Pod);
        if (status.equals("Running")) {
            PodUpdateDTO podUpdateDTO = new PodUpdateDTO();
            Optional<V1Container> container = v1Pod.getSpec().getContainers().stream().filter(v1Container -> v1Container.getName().contains("automation-test")).findFirst();
            if (container.isPresent()) {
                podUpdateDTO.setConName(container.get().getName());
            }
            podUpdateDTO.setPodName(v1Pod.getMetadata().getName());
            podUpdateDTO.setReleaseNames(KeyParseTool.getReleaseName(key));
            podUpdateDTO.setStatus(0L);
            String input = gson.toJson(podUpdateDTO);
            logger.info(input);
            sagaClient.startSaga("test-pod-update-saga", new StartInstanceDTO(input, "", ""));
        }
    }

    @Override
    @Saga(code = "test-job-log-saga",
            description = "测试Job日志(test job log saga)", inputSchema = "{}")
    public void testJobLog(String key, String msg, Long clusterId) {
        JobLogDTO jobLogDTO = json.deserialize(msg, JobLogDTO.class);
        PodUpdateDTO podUpdateDTO = new PodUpdateDTO();
        podUpdateDTO.setReleaseNames(KeyParseTool.getReleaseName(key));
        if (jobLogDTO.getSucceed() != null && jobLogDTO.getSucceed()) {
            podUpdateDTO.setStatus(1L);
        } else {
            podUpdateDTO.setStatus(-1L);
        }
        podUpdateDTO.setLogFile(jobLogDTO.getLog());
        String input = gson.toJson(podUpdateDTO);
        logger.info(input);
        sagaClient.startSaga("test-job-log-saga", new StartInstanceDTO(input, "", ""));
    }

    @Override
    @Saga(code = "test-status-saga",
            description = "测试Release状态(test status saga)", inputSchema = "{}")
    public void getTestAppStatus(String key, String msg, Long clusterId) {
        logger.info(msg);
        List<TestReleaseStatus> testReleaseStatuses = JSONArray.parseArray(msg, TestReleaseStatus.class);
        List<PodUpdateDTO> podUpdateDTOS = new ArrayList<>();
        for (TestReleaseStatus testReleaseStatu : testReleaseStatuses) {
            PodUpdateDTO podUpdateDTO = new PodUpdateDTO();
            podUpdateDTO.setReleaseNames(testReleaseStatu.getReleaseName());
            if (testReleaseStatu.getStatus().equals("running")) {
                podUpdateDTO.setStatus(1L);
            } else {
                podUpdateDTO.setStatus(0L);
            }
            podUpdateDTOS.add(podUpdateDTO);
        }
        String input = JSONArray.toJSONString(podUpdateDTOS);
        sagaClient.startSaga("test-status-saga", new StartInstanceDTO(input, "", ""));
    }

    @Override
    public void getCertManagerInfo(String payloadMsg, Long clusterId) {
        if (payloadMsg == null) {
            Msg msg = new Msg();
            Payload payload = new Payload(
                    "kube-system",
                    certManagerUrl,
                    "cert-manager",
                    "0.1.0",
                    null, "choerodon-cert-manager", null);
            msg.setKey(String.format("cluster:%d.release:%s",
                    clusterId,
                    "choerodon-cert-manager"));
            msg.setType(HelmType.HELM_INSTALL_RELEASE.toValue());
            try {
                msg.setPayload(mapper.writeValueAsString(payload));
            } catch (IOException e) {
                throw new CommonException("error.payload.error", e);
            }
            socketMsgDispatcher.dispatcher(msg);
        }
    }


    private void updateResourceStatus(Long envId, DevopsEnvCommandE devopsEnvCommandE, InstanceStatus running, ServiceStatus running2, IngressStatus running3, CertificationStatus active) {
        if (devopsEnvCommandE.getObject().equals(INSTANCE_KIND)) {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(devopsEnvCommandE.getObjectId());
            applicationInstanceE.setStatus(running.getStatus());
            applicationInstanceRepository.update(applicationInstanceE);
        }
        if (devopsEnvCommandE.getObject().equals(SERVICE_KIND)) {
            DevopsServiceE devopsServiceE = devopsServiceRepository.query(devopsEnvCommandE.getObjectId());
            devopsServiceE.setStatus(running2.getStatus());
            devopsServiceRepository.update(devopsServiceE);
        }
        if (devopsEnvCommandE.getObject().equals(INGRESS_KIND)) {
            DevopsIngressDO devopsIngressDO = devopsIngressRepository.getIngress(devopsEnvCommandE.getObjectId());
            devopsIngressRepository.setStatus(envId, devopsIngressDO.getName(), running3.getStatus());
        }
        if (devopsEnvCommandE.getObject().equals(CERTIFICATE_KIND)) {
            CertificationE certificationE = certificationRepository.queryById(devopsEnvCommandE.getObjectId());
            certificationE.setStatus(active.getStatus());
            certificationRepository.updateStatus(certificationE);
        }
    }

    @Override
    public void certIssued(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }

        try {
            String certName = KeyParseTool.getValue(key, "Cert");
            CertificationE certificationE = certificationRepository.queryByEnvAndName(envId, certName);
            if (certificationE != null) {
                DevopsEnvCommandE commandE = devopsEnvCommandRepository.query(certificationE.getCommandId());
                Object obj = objectMapper.readValue(msg, Object.class);
                String crt = ((LinkedHashMap) ((LinkedHashMap) obj).get("data")).get("tls.crt").toString();
                X509Certificate certificate = CertificateUtil.decodeCert(Base64Util.base64Decoder(crt));
                Date validFrom = certificate.getNotBefore();
                Date validUntil = certificate.getNotAfter();
                if (!(validFrom.equals(certificationE.getValidFrom())
                        && validUntil.equals(certificationE.getValidUntil()))) {
                    certificationE.setValid(validFrom, validUntil);
                    certificationRepository.updateValid(certificationE);
                }
                boolean commandNotExist = commandE == null;
                if (commandNotExist) {
                    commandE = new DevopsEnvCommandE();
                }
                commandE.setObject(ObjectType.CERTIFICATE.getType());
                commandE.setCommandType(CommandType.CREATE.getType());
                commandE.setObjectId(certificationE.getId());
                commandE.setStatus(CommandStatus.SUCCESS.getStatus());
                commandE.setSha(KeyParseTool.getValue(key, "commit"));
                if (commandNotExist) {
                    commandE = devopsEnvCommandRepository.create(commandE);
                } else {
                    devopsEnvCommandRepository.update(commandE);
                }
                certificationE.setCommandId(commandE.getId());
                certificationRepository.updateCommandId(certificationE);
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
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }

        String commitSha = KeyParseTool.getValue(key, "commit");
        String certName = KeyParseTool.getValue(key, "Cert");
        CertificationE certificationE = certificationRepository.queryByEnvAndName(envId, certName);
        if (certificationE != null) {
            DevopsEnvCommandE commandE = devopsEnvCommandRepository.query(certificationE.getCommandId());
            String createType = CommandType.CREATE.getType();
            String commandFailedStatus = CommandStatus.FAILED.getStatus();
            boolean commandNotExist = commandE == null;
            if (commandNotExist) {
                commandE = new DevopsEnvCommandE();
            }
            if (!createType.equals(commandE.getCommandType())
                    || !commandFailedStatus.equals(commandE.getStatus())
                    || (!msg.isEmpty() && !msg.equals(commandE.getError()))) {
                commandE.setObject(ObjectType.CERTIFICATE.getType());
                commandE.setCommandType(createType);
                commandE.setObjectId(certificationE.getId());
                commandE.setStatus(commandFailedStatus);
                commandE.setSha(commitSha);
                commandE.setError(msg);
                if (commandNotExist) {
                    commandE = devopsEnvCommandRepository.create(commandE);
                } else {
                    devopsEnvCommandRepository.update(commandE);
                }
                certificationE.setCommandId(commandE.getId());
                certificationRepository.updateCommandId(certificationE);
            }
            certificationRepository.clearValid(certificationE.getId());
            String failedStatus = CertificationStatus.FAILED.getStatus();
            if (failedStatus.equals(certificationE.getStatus())) {
                certificationE.setStatus(failedStatus);
                certificationRepository.updateStatus(certificationE);
            }
        }

    }


    private Long getEnvId(String key, Long clusterId) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryByClusterIdAndCode(clusterId, KeyParseTool.getNamespace(key));
        Long envId = null;
        if (devopsEnvironmentE != null) {
            envId = devopsEnvironmentE.getId();
        }
        return envId;
    }

    @Override
    public void handleNodeSync(String msg, Long clusterId) {
        clusterNodeInfoService.setValueForKey(clusterNodeInfoService.getRedisClusterKey(clusterId), JSONArray.parseArray(msg, AgentNodeInfoDTO.class));
    }

    @Override
    public void handleConfigUpdate(String key, String msg, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        V1ConfigMap v1ConfigMap = json.deserialize(msg, V1ConfigMap.class);
        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryByEnvIdAndName(envId, v1ConfigMap.getMetadata().getName());
        if (devopsConfigMapE == null) {
            DevopsConfigMapDTO devopsConfigMapDTO = new DevopsConfigMapDTO();
            devopsConfigMapDTO.setDescription(v1ConfigMap.getMetadata().getName() + " config");
            devopsConfigMapDTO.setEnvId(envId);
            devopsConfigMapDTO.setName(v1ConfigMap.getMetadata().getName());
            devopsConfigMapDTO.setType("create");
            devopsConfigMapDTO.setValue(v1ConfigMap.getData());
            devopsConfigMapService.createOrUpdate(devopsEnvironmentE.getProjectE().getId(), true, devopsConfigMapDTO);
        }
    }

    @Override
    public void operateDockerRegistrySecretResp(String key, String result, Long clusterId) {
        Long envId = getEnvId(key, clusterId);
        if (envId == null) {
            logger.info(ENV_NOT_EXIST, KeyParseTool.getNamespace(key));
            return;
        }
        DevopsRegistrySecretE devopsRegistrySecretE = devopsRegistrySecretRepository.queryByName(envId, KeyParseTool.getResourceName(key));
        if (result.equals("failed")) {
            devopsRegistrySecretE.setStatus(false);
        } else {
            devopsRegistrySecretE.setStatus(true);
        }
        devopsRegistrySecretRepository.update(devopsRegistrySecretE);
    }
}

