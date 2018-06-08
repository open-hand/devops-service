package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DeployMsgHandlerService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.factory.*;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.*;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.K8sUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.*;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodContainerDO;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.devops.infra.mapper.DevopsIngressMapper;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.process.SocketMsgDispatcher;
import io.choerodon.websocket.tool.KeyParseTool;

/**
 * Created by Zenger on 2018/4/17.
 */
@Service
public class DeployMsgHandlerServiceImpl implements DeployMsgHandlerService {

    private static final String SERVICE_LABLE = "choerodon.io/network";
    private static final String PENDING = "Pending";
    private static final String METADATA = "metadata";
    private static final Logger logger = LoggerFactory.getLogger(DeployMsgHandlerServiceImpl.class);
    private static final String RESOURCEVERSION = "resourceVersion";
    private static JSON json = new JSON();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private ObjectMapper mapper = new ObjectMapper();
    @Value("${services.helm.url}")
    private String helmUrl;

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
    private ApplicationVersionValueRepository applicationVersionValueRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;
    @Autowired
    private DevopsEnvPodContainerRepository containerRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsEnvCommandValueRepository devopsEnvCommandValueRepository;
    @Autowired
    private DevopsIngressMapper devopsIngressMapper;
    @Autowired
    private SocketMsgDispatcher socketMsgDispatcher;

    /**
     * pod 更新
     *
     * @param key   消息key
     * @param envId 环境Id
     * @param msg   消息msg
     */
    public void handlerUpdateMessage(String key, Long envId, String msg) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);
        Msg msg1 = new Msg();
        ApplicationInstanceE applicationInstanceE =
                applicationInstanceRepository.selectByCode(KeyParseTool.getReleaseName(key));
        if (applicationInstanceE == null) {
            Payload payload = new Payload(
                    null,
                    null,
                    null,
                    null,
                    null,
                    KeyParseTool.getReleaseName(key));
            msg1.setKey("env:" + KeyParseTool.getNamespace(key) + ".envId:" + envId + ".release:" + KeyParseTool.getReleaseName(key));
            msg1.setType(HelmType.HelmReleaseGetContent.toValue());
            try {
                msg1.setPayload(mapper.writeValueAsString(payload));
            } catch (IOException e) {
                throw new CommonException("error.payload.error");
            }
            socketMsgDispatcher.dispatcher(msg1);
            return;
        }
        DevopsEnvResourceE devopsEnvResourceE = DevopsInstanceResourceFactory.createDevopsInstanceResourceE();
        DevopsEnvResourceE newDevopsEnvResourceE =
                devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                        applicationInstanceE.getId(),
                        KeyParseTool.getResourceType(key),
                        v1Pod.getMetadata().getName());
        DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
        devopsEnvResourceDetailE.setMessage(msg);
        devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
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
        if (v1OwnerReferences.get(0).getKind().equals(ResourceType.REPLICASET.getType())) {
            String status = K8sUtil.changePodStatus(v1Pod);
            String resourceVersion = v1Pod.getMetadata().getResourceVersion();

            DevopsEnvPodE devopsEnvPodE = new DevopsEnvPodE();
            devopsEnvPodE.setName(v1Pod.getMetadata().getName());
            devopsEnvPodE.setIp(v1Pod.getStatus().getPodIP());
            devopsEnvPodE.setStatus(status);
            devopsEnvPodE.setResourceVersion(resourceVersion);
            devopsEnvPodE.setNamespace(v1Pod.getMetadata().getNamespace());
            if (!PENDING.equals(status)) {
                devopsEnvPodE.setReady(v1Pod.getStatus().getContainerStatuses().get(0).isReady());
            } else {
                devopsEnvPodE.setReady(false);
            }

            Boolean flag = false;
            if (applicationInstanceE.getId() != null) {
                List<DevopsEnvPodE> devopsEnvPodEList = devopsEnvPodRepository
                        .selectByInstanceId(applicationInstanceE.getId());
                if (devopsEnvPodEList == null || devopsEnvPodEList.isEmpty()) {
                    devopsEnvPodE.initApplicationInstanceE(applicationInstanceE.getId());
                    devopsEnvPodRepository.insert(devopsEnvPodE);
                    Long podId = devopsEnvPodRepository.get(devopsEnvPodE).getId();
                    v1Pod.getSpec().getContainers().parallelStream().forEach(t ->
                            containerRepository.insert(new DevopsEnvPodContainerDO(
                                    podId,
                                    t.getName())));
                } else {
                    for (DevopsEnvPodE pod : devopsEnvPodEList) {
                        if (pod.getName().equals(v1Pod.getMetadata().getName())
                                && pod.getNamespace().equals(v1Pod.getMetadata().getNamespace())) {
                            if (!resourceVersion.equals(pod.getResourceVersion())) {
                                devopsEnvPodE.setId(pod.getId());
                                devopsEnvPodE.initApplicationInstanceE(pod.getApplicationInstanceE().getId());
                                devopsEnvPodE.setObjectVersionNumber(pod.getObjectVersionNumber());
                                devopsEnvPodRepository.update(devopsEnvPodE);
                                containerRepository.deleteByPodId(pod.getId());
                                v1Pod.getSpec().getContainers().parallelStream().forEach(t ->
                                        containerRepository.insert(
                                                new DevopsEnvPodContainerDO(pod.getId(), t.getName())));
                            }
                            flag = true;
                        }
                    }
                    if (!flag) {
                        devopsEnvPodE.initApplicationInstanceE(applicationInstanceE.getId());
                        devopsEnvPodRepository.insert(devopsEnvPodE);
                        Long podId = devopsEnvPodRepository.get(devopsEnvPodE).getId();
                        v1Pod.getSpec().getContainers().parallelStream().forEach(t ->
                                containerRepository.insert(new DevopsEnvPodContainerDO(
                                        podId,
                                        t.getName())));
                    }
                }
            }
        }
    }

    @Override
    public void handlerReleaseInstall(String msg) {
        ReleasePayload releasePayload = JSONArray.parseObject(msg, ReleasePayload.class);
        List<Resource> resources = JSONArray.parseArray(releasePayload.getResources(), Resource.class);
        String releaseName = releasePayload.getName();
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(releaseName);
        applicationInstanceE.setStatus(InstanceStatus.RUNNING.getStatus());
        applicationInstanceRepository.update(applicationInstanceE);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INSTANCE.getObjectType(), applicationInstanceE.getId());
        devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getCommandStatus());
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        installResource(resources, applicationInstanceE);
    }


    @Override
    public void handlerPreInstall(String msg) {
        if (msg.equals("null")) {
            return;
        }
        List<Job> jobs = JSONArray.parseArray(msg, Job.class);
        try {
            for (Job job : jobs) {
                ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                        .selectByCode(job.getReleaseName());
                DevopsEnvResourceE newdevopsEnvResourceE =
                        devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                                applicationInstanceE.getId(),
                                job.getKind(),
                                job.getName());
                DevopsEnvResourceE devopsEnvResourceE =
                        DevopsInstanceResourceFactory.createDevopsInstanceResourceE();
                devopsEnvResourceE.setKind(job.getKind());
                devopsEnvResourceE.setName(job.getName());
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
        } catch (Exception e) {
            throw new CommonException("error.resource.insert");
        }
    }

    @Override
    public void resourceUpdate(String key, Long envId, String msg) {
        try {
            Object obj = objectMapper.readValue(msg, Object.class);
            DevopsEnvResourceE devopsEnvResourceE =
                    DevopsInstanceResourceFactory.createDevopsInstanceResourceE();
            DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
            devopsEnvResourceDetailE.setMessage(msg);
            devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
            devopsEnvResourceE.setName(
                    KeyParseTool.getResourceName(key));
            devopsEnvResourceE.setReversion(
                    TypeUtil.objToLong(
                            ((LinkedHashMap) ((LinkedHashMap) obj).get(METADATA)).get(RESOURCEVERSION).toString()));
            String releaseName = null;
            DevopsEnvResourceE newdevopsEnvResourceE = null;
            ApplicationInstanceE applicationInstanceE = null;
            ResourceType resourceType = ResourceType.forString(KeyParseTool.getResourceType(key));
            switch (resourceType) {
                case INGRESS:
                    V1beta1Ingress v1beta1Ingress = json.deserialize(msg, V1beta1Ingress.class);
                    Map<String, String> label = v1beta1Ingress.getMetadata().getLabels();
                    if (label.get(SERVICE_LABLE) != null
                            && label.get(SERVICE_LABLE).equals("ingress")) {
                        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
                        if (devopsEnvironmentE == null) {
                            return;
                        }
                        DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(
                                devopsEnvironmentE.getId(), v1beta1Ingress.getMetadata().getName());
                        if (devopsIngressE == null) {
                            devopsIngressE = new DevopsIngressE();
                            devopsIngressE.setProjectId(devopsEnvironmentE.getProjectE().getId());
                            devopsIngressE.setDomain(v1beta1Ingress.getSpec().getRules().get(0).getHost());
                            devopsIngressE.setEnvId(devopsEnvironmentE.getId());
                            devopsIngressE.setName(v1beta1Ingress.getMetadata().getName());
                            devopsIngressE.setUsable(true);
                            devopsIngressE.setStatus(IngressStatus.RUNNING.getStatus());
                            devopsIngressE = devopsIngressRepository.insertIngress(devopsIngressE);
                            DevopsEnvCommandE devopsEnvCommandE = DevopsEnvCommandFactory.createDevopsEnvCommandE();
                            devopsEnvCommandE.setObject(ObjectType.INGRESS.getObjectType());
                            devopsEnvCommandE.setObjectId(devopsIngressE.getId());
                            devopsEnvCommandE.setCommandType(CommandType.CREATE.getCommandType());
                            devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getCommandStatus());
                            devopsEnvCommandRepository.create(devopsEnvCommandE);
                            List<V1beta1HTTPIngressPath> paths = v1beta1Ingress.getSpec().getRules()
                                    .get(0).getHttp().getPaths();
                            for (V1beta1HTTPIngressPath path : paths) {
                                DevopsIngressPathE devopsIngressPathE = new DevopsIngressPathE();
                                devopsIngressPathE.setDevopsIngressE(devopsIngressE);
                                devopsIngressPathE.setServiceName(path.getBackend().getServiceName());
                                DevopsServiceE devopsServiceE = devopsServiceRepository
                                        .selectByNameAndNamespace(path.getBackend().getServiceName(),
                                                v1beta1Ingress.getMetadata().getNamespace());
                                if (devopsServiceE != null) {
                                    devopsIngressPathE.setServiceId(devopsServiceE.getId());
                                }
                                devopsIngressPathE.setPath(path.getPath());
                                devopsIngressRepository.insertIngressPath(devopsIngressPathE);
                            }
                        }
                    }

                    newdevopsEnvResourceE =
                            devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                                    null,
                                    KeyParseTool.getResourceType(key),
                                    KeyParseTool.getResourceName(key));
                    saveOrUpdateResource(devopsEnvResourceE, newdevopsEnvResourceE,
                            devopsEnvResourceDetailE, null);
                    break;
                case POD:
                    handlerUpdateMessage(key, envId, msg);
                    break;
                case SERVICE:
                    V1Service v1Service = json.deserialize(msg, V1Service.class);
                    String releaseNames = v1Service.getMetadata().getAnnotations().get("choerodon.io/network-service-instances");
                    List<String> releases = Arrays.asList(releaseNames.split("\\+"));

                    Boolean flag = false;
                    Map<String, String> lab = v1Service.getMetadata().getLabels();
                    if (lab.get(SERVICE_LABLE) != null && lab.get(SERVICE_LABLE).equals("service")) {
                        flag = true;
                    }
                    for (String release : releases) {
                        applicationInstanceE = applicationInstanceRepository
                                .selectByCode(release);

                        String namespace = v1Service.getMetadata().getNamespace();
                        if (flag) {
                            DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(
                                    v1Service.getMetadata().getName(), namespace);
                            if (devopsServiceE == null) {
                                if (applicationInstanceE != null) {
                                    devopsServiceE = new DevopsServiceE();
                                    devopsServiceE.setEnvId(applicationInstanceE.getDevopsEnvironmentE().getId());
                                    devopsServiceE.setAppId(applicationInstanceE.getApplicationE().getId());
                                    devopsServiceE.setName(KeyParseTool.getResourceName(key));
                                    devopsServiceE.setNamespace(namespace);
                                    devopsServiceE.setStatus(ServiceStatus.RUNNING.getStatus());
                                    devopsServiceE.setPort(v1Service.getSpec().getPorts().get(0).getPort().longValue());
                                    devopsServiceE.setTargetPort(v1Service.getSpec().getPorts().get(0)
                                            .getTargetPort().getIntValue().longValue());
                                    if (v1Service.getSpec().getExternalIPs() != null) {
                                        devopsServiceE.setExternalIp(v1Service.getSpec().getExternalIPs().get(0));
                                    }
                                    devopsServiceE.setLabel(json.serialize(lab));
                                    devopsServiceE = devopsServiceRepository.insert(devopsServiceE);

                                    DevopsServiceAppInstanceE devopsServiceAppInstanceE = devopsServiceInstanceRepository
                                            .queryByOptions(devopsServiceE.getId(), applicationInstanceE.getId());
                                    if (devopsServiceAppInstanceE == null) {
                                        devopsServiceAppInstanceE = new DevopsServiceAppInstanceE();
                                        devopsServiceAppInstanceE.setServiceId(devopsServiceE.getId());
                                        devopsServiceAppInstanceE.setAppInstanceId(applicationInstanceE.getId());
                                        devopsServiceAppInstanceE.setCode(release);
                                        devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
                                    }

                                    DevopsEnvCommandE devopsEnvCommandE = DevopsEnvCommandFactory.createDevopsEnvCommandE();
                                    devopsEnvCommandE.setObject(ObjectType.SERVICE.getObjectType());
                                    devopsEnvCommandE.setObjectId(devopsServiceE.getId());
                                    devopsEnvCommandE.setCommandType(CommandType.CREATE.getCommandType());
                                    devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getCommandStatus());
                                    devopsEnvCommandRepository.create(devopsEnvCommandE);
                                    DevopsEnvResourceE newdevopsInsResourceE =
                                            devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                                                    applicationInstanceE.getId(),
                                                    KeyParseTool.getResourceType(key),
                                                    KeyParseTool.getResourceName(key));
                                    saveOrUpdateResource(devopsEnvResourceE, newdevopsInsResourceE,
                                            devopsEnvResourceDetailE, applicationInstanceE);
                                }
                            }

                            List<DevopsIngressPathE> devopsIngressPathEList = devopsIngressRepository.selectByEnvIdAndServiceName(
                                    devopsServiceE.getEnvId(), devopsServiceE.getName());
                            for (DevopsIngressPathE dd : devopsIngressPathEList) {
                                if (dd.getServiceId() == null) {
                                    dd.setServiceId(devopsServiceE.getId());
                                    devopsIngressRepository.updateIngressPath(dd);
                                }
                            }
                        }
                    }
                    break;
                default:
                    releaseName = KeyParseTool.getReleaseName(key);
                    applicationInstanceE = applicationInstanceRepository.selectByCode(releaseName);
                    if (applicationInstanceE == null) {
                        return;
                    }
                    DevopsEnvResourceE newdevopsInsResourceE =
                            devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                                    applicationInstanceE.getId(),
                                    KeyParseTool.getResourceType(key),
                                    KeyParseTool.getResourceName(key));
                    saveOrUpdateResource(devopsEnvResourceE, newdevopsInsResourceE,
                            devopsEnvResourceDetailE, applicationInstanceE);
                    break;
            }
        } catch (IOException e) {
            logger.info(e.toString());
        }
    }

    @Override
    public void resourceDelete(Long envId, String msg) {
        if (!KeyParseTool.getResourceType(msg).equals(ResourceType.JOB.getType())) {
            if (KeyParseTool.getResourceType(msg).equals(ResourceType.POD.getType())) {
                String podName = KeyParseTool.getResourceName(msg);
                String podNameSpace = KeyParseTool.getNamespace(msg);
                DevopsEnvPodE podE = devopsEnvPodRepository.get(new DevopsEnvPodE(podName, podNameSpace));
                devopsEnvPodRepository.deleteByName(podName, podNameSpace);
                if (podE != null) {
                    containerRepository.deleteByPodId(podE.getId());
                }
            }

            if (KeyParseTool.getResourceType(msg).equals(ResourceType.SERVICE.getType())) {
                DevopsServiceE devopsServiceE =
                        devopsServiceRepository.selectByNameAndNamespace(
                                KeyParseTool.getResourceName(msg),
                                KeyParseTool.getNamespace(msg));
                //更新网络数据
                if (devopsServiceE != null) {
                    devopsServiceE.setStatus(ServiceStatus.DELETED.getStatus());
                    devopsServiceRepository.update(devopsServiceE);

                    DevopsEnvCommandE newdevopsEnvCommandE = devopsEnvCommandRepository
                            .queryByObject(ObjectType.SERVICE.getObjectType(), devopsServiceE.getId());
                    newdevopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getCommandStatus());
                    devopsEnvCommandRepository.update(newdevopsEnvCommandE);
                }
            }
            if (KeyParseTool.getResourceType(msg).equals(ResourceType.INGRESS.getType())) {
                DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
                DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(
                        devopsEnvironmentE.getId(), KeyParseTool.getResourceName(msg));
                if (devopsIngressE != null) {
                    devopsIngressRepository.deleteIngress(devopsIngressE.getId());
                    devopsIngressRepository.deleteIngressPath(devopsIngressE.getId());
                }
                DevopsIngressDO domainDO = devopsIngressMapper
                        .select(new DevopsIngressDO(KeyParseTool.getResourceName(msg))).get(0);
                DevopsEnvCommandE newdevopsEnvCommandE = devopsEnvCommandRepository
                        .queryByObject(ObjectType.INGRESS.getObjectType(), domainDO.getId());
                newdevopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getCommandStatus());
                devopsEnvCommandRepository.update(newdevopsEnvCommandE);
            }

            devopsEnvResourceRepository.deleteByKindAndName(
                    KeyParseTool.getResourceType(msg),
                    KeyParseTool.getResourceName(msg));
        }
    }

    @Override
    public void helmReleaseHookLogs(String key, String msg) {
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                .selectByCode(KeyParseTool.getReleaseName(key));
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INSTANCE.getObjectType(), applicationInstanceE.getId());
        DevopsEnvCommandLogE devopsEnvCommandLogE = new DevopsEnvCommandLogE();
        devopsEnvCommandLogE.initDevopsEnvCommandE(devopsEnvCommandE.getId());
        devopsEnvCommandLogE.setLog(msg);
        devopsEnvCommandLogRepository.create(devopsEnvCommandLogE);
    }

    @Override
    public void updateInstanceStatus(String key, String instanceStatus, String commandStatus, String msg) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectByCode(key);
        if (instanceE != null) {
            instanceE.setStatus(instanceStatus);
            applicationInstanceRepository.update(instanceE);
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INSTANCE.getObjectType(), instanceE.getId());
            devopsEnvCommandE.setStatus(commandStatus);
            devopsEnvCommandE.setError(msg);
            devopsEnvCommandRepository.update(devopsEnvCommandE);
        }
    }

    @Override
    public void handlerDomainCreateMessage(String key, String msg, Long envId) {
        V1beta1Ingress ingress = json.deserialize(msg, V1beta1Ingress.class);
        DevopsEnvResourceE devopsEnvResourceE = DevopsInstanceResourceFactory.createDevopsInstanceResourceE();
        DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
        devopsEnvResourceDetailE.setMessage(msg);
        devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
        devopsEnvResourceE.setName(KeyParseTool.getResourceName(key));
        devopsEnvResourceE.setReversion(TypeUtil.objToLong(ingress.getMetadata().getResourceVersion()));
        DevopsEnvResourceE newDevopsEnvResourceE =
                devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                        null,
                        KeyParseTool.getResourceType(key),
                        KeyParseTool.getResourceName(key));

        saveOrUpdateResource(devopsEnvResourceE, newDevopsEnvResourceE, devopsEnvResourceDetailE, null);
        String ingressName = ingress.getMetadata().getName();
        devopsIngressRepository.setUsable(ingressName);
        Long ingressId = devopsIngressRepository.setStatus(envId, ingressName, IngressStatus.RUNNING.getStatus());
        DevopsEnvCommandE commandE =
                devopsEnvCommandRepository.queryByObject(ObjectType.INGRESS.getObjectType(), ingressId);
        commandE.setStatus(CommandStatus.SUCCESS.getCommandStatus());
        devopsEnvCommandRepository.update(commandE);
    }

    @Override
    public void helmReleasePreUpgrade(String msg) {
        handlerPreInstall(msg);
    }

    @Override
    public void handlerReleaseUpgrade(String msg) {
        handlerReleaseInstall(msg);
    }


    public ApplicationInstanceE helmRelease(Long envId, String msg) {
        ReleasePayload releasePayload = JSONArray.parseObject(msg, ReleasePayload.class);
        ApplicationVersionValueE applicationVersionValueE = ApplicationVersionValueFactory.create();
        ApplicationVersionE applicationVersionE = ApplicationVersionEFactory.create();
        if (applicationInstanceRepository.selectByCode(releasePayload.getName()) == null) {
            try {
                ApplicationInstanceE applicationInstanceE = ApplicationInstanceFactory.create();
                DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                        .queryById(envId);
                ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
                Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
                ApplicationE applicationE = applicationRepository
                        .queryByCode(releasePayload.getChartName(), devopsEnvironmentE.getProjectE().getId());
                applicationVersionE.initApplicationEById(applicationE.getId());
                String image = String.format("%s%s%s%s%s%s%s%s%s", harborConfigurationProperties.getBaseUrl(),
                        System.getProperty("file.separator"),
                        organization.getCode(),
                        "-",
                        projectE.getCode(),
                        System.getProperty("file.separator"),
                        applicationE.getCode(),
                        ":",
                        releasePayload.getChartVersion()
                );
                applicationVersionE.setImage(image);
                applicationVersionE.setVersion(releasePayload.getChartVersion());
                applicationVersionE
                        .setRepository("/" + organization.getCode() + "/" + projectE.getCode() + "/");
                applicationVersionValueE.setValue(releasePayload.getConfig());
                applicationInstanceE.setCode(releasePayload.getName());
                applicationInstanceE.setStatus(InstanceStatus.RUNNING.getStatus());
                applicationInstanceE.initApplicationEById(applicationE.getId());
                ApplicationVersionE newApplicationVersionE = applicationVersionRepository
                        .queryByAppAndVersion(applicationE.getId(), releasePayload.getChartVersion());
                DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
                if (newApplicationVersionE == null) {
                    applicationVersionE.initApplicationVersionValueE(
                            applicationVersionValueRepository.create(applicationVersionValueE).getId());
                    applicationInstanceE.initApplicationVersionEById(
                            applicationVersionRepository.create(applicationVersionE).getId());
                } else {
                    applicationInstanceE.initApplicationVersionEById(newApplicationVersionE.getId());
                }
                applicationInstanceE.initDevopsEnvironmentEById(devopsEnvironmentE.getId());
                DevopsEnvCommandValueE devopsEnvCommandValueE = DevopsEnvCommandValueFactory
                        .createDevopsEnvCommandE();
                devopsEnvCommandValueE.setValue(releasePayload.getConfig());
                devopsEnvCommandE.setObject(ObjectType.INSTANCE.getObjectType());
                devopsEnvCommandE.setCommandType(CommandType.CREATE.getCommandType());
                devopsEnvCommandE.setObjectId(applicationInstanceRepository.create(applicationInstanceE).getId());
                devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getCommandStatus());
                devopsEnvCommandE.initDevopsEnvCommandValueE(
                        devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
                devopsEnvCommandRepository.create(devopsEnvCommandE);
                applicationInstanceE.setId(devopsEnvCommandE.getObjectId());
                return applicationInstanceE;
            } catch (Exception e) {
                throw new CommonException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void helmReleaseDeleteFail(String key, String msg) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                InstanceStatus.DELETED.getStatus(),
                CommandStatus.FAILED.getCommandStatus(),
                msg);
    }

    @Override
    public void helmReleaseStartFail(String key, String msg) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                InstanceStatus.STOPED.getStatus(),
                CommandStatus.FAILED.getCommandStatus(),
                msg);
    }

    @Override
    public void helmReleaseRollBackFail(String key, String msg) {
        return;
    }

    @Override
    public void helmReleaseInstallFail(String key, String msg) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                InstanceStatus.FAILED.getStatus(),
                CommandStatus.FAILED.getCommandStatus(),
                msg);
    }

    @Override
    public void helmReleaseUpgradeFail(String key, String msg) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getCommandStatus(),
                msg);
    }

    @Override
    public void helmReleaeStopFail(String key, String msg) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getCommandStatus(),
                msg);

    }

    @Override
    public void netWorkServiceFail(String key, String msg) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(KeyParseTool.getValue(key, "Service"), KeyParseTool.getValue(key, "env"));
        devopsServiceE.setStatus(ServiceStatus.FAILED.getStatus());
        devopsServiceRepository.update(devopsServiceE);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.SERVICE.getObjectType(), devopsServiceE.getId());
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getCommandStatus());
        devopsEnvCommandE.setError(msg);
        devopsEnvCommandRepository.update(devopsEnvCommandE);
    }

    @Override
    public void netWorkIngressFail(String key, Long envId, String msg) {
        DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, KeyParseTool.getValue(key, "Ingress"));
        devopsIngressE.setStatus(IngressStatus.FAILED.getStatus());
        devopsIngressRepository.updateIngress(ConvertHelper.convert(devopsIngressE, DevopsIngressDO.class));
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INGRESS.getObjectType(), devopsIngressE.getId());
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getCommandStatus());
        devopsEnvCommandE.setError(msg);
        devopsEnvCommandRepository.update(devopsEnvCommandE);
    }

    @Override
    public void netWorkServiceDeleteFail(String key, String msg) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(KeyParseTool.getValue(key, "Service"), KeyParseTool.getValue(key, "env"));
        devopsServiceE.setStatus(ServiceStatus.DELETED.getStatus());
        devopsServiceRepository.update(devopsServiceE);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.SERVICE.getObjectType(), devopsServiceE.getId());
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getCommandStatus());
        devopsEnvCommandE.setError(msg);
        devopsEnvCommandRepository.update(devopsEnvCommandE);
    }

    @Override
    public void netWorkIngressDeleteFail(String key, Long envId, String msg) {
        DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, KeyParseTool.getValue(key, "Ingress"));
        devopsIngressRepository.deleteIngress(devopsIngressE.getId());
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.INGRESS.getObjectType(), devopsIngressE.getId());
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getCommandStatus());
        devopsEnvCommandE.setError(msg);
        devopsEnvCommandRepository.update(devopsEnvCommandE);
    }

    @Override
    public void netWorkUpdate(String key, String msg) {
        V1Service v1Service = json.deserialize(msg, V1Service.class);

        String releaseNames = v1Service.getMetadata().getAnnotations().get("choerodon.io/network-service-instances");
        List<String> releases = Arrays.asList(releaseNames.split("\\+"));

        DevopsEnvResourceE devopsEnvResourceE =
                DevopsInstanceResourceFactory.createDevopsInstanceResourceE();
        DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
        devopsEnvResourceDetailE.setMessage(msg);
        devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
        devopsEnvResourceE.setName(
                KeyParseTool.getResourceName(key));
        devopsEnvResourceE.setReversion(
                TypeUtil.objToLong(v1Service.getMetadata().getResourceVersion()));

        Boolean flag = false;
        Map<String, String> label = v1Service.getMetadata().getLabels();
        if (label.get(SERVICE_LABLE) != null && label.get(SERVICE_LABLE).equals("service")) {
            flag = true;
        }
        for (String release : releases) {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                    .selectByCode(release);

            String namespace = v1Service.getMetadata().getNamespace();
            if (flag) {
                DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(
                        v1Service.getMetadata().getName(), namespace);
                if (devopsServiceE == null) {
                    devopsServiceE = new DevopsServiceE();
                    devopsServiceE.setEnvId(applicationInstanceE.getDevopsEnvironmentE().getId());
                    devopsServiceE.setAppId(applicationInstanceE.getApplicationE().getId());
                    devopsServiceE.setName(KeyParseTool.getResourceName(key));
                    devopsServiceE.setNamespace(namespace);
                    devopsServiceE.setStatus(ServiceStatus.RUNNING.getStatus());
                    devopsServiceE.setPort(v1Service.getSpec().getPorts().get(0).getPort().longValue());
                    devopsServiceE.setTargetPort(v1Service.getSpec().getPorts().get(0)
                            .getTargetPort().getIntValue().longValue());
                    if (v1Service.getSpec().getExternalIPs() != null) {
                        devopsServiceE.setExternalIp(v1Service.getSpec().getExternalIPs().get(0));
                    }
                    devopsServiceE.setLabel(json.serialize(label));
                    devopsServiceE = devopsServiceRepository.insert(devopsServiceE);

                    DevopsServiceAppInstanceE devopsServiceAppInstanceE = devopsServiceInstanceRepository
                            .queryByOptions(devopsServiceE.getId(), applicationInstanceE.getId());
                    if (devopsServiceAppInstanceE == null) {
                        devopsServiceAppInstanceE = new DevopsServiceAppInstanceE();
                        devopsServiceAppInstanceE.setServiceId(devopsServiceE.getId());
                        devopsServiceAppInstanceE.setAppInstanceId(applicationInstanceE.getId());
                        devopsServiceAppInstanceE.setCode(release);
                        devopsServiceInstanceRepository.insert(devopsServiceAppInstanceE);
                    }

                    DevopsEnvCommandE devopsEnvCommandE = DevopsEnvCommandFactory.createDevopsEnvCommandE();
                    devopsEnvCommandE.setObject(ObjectType.SERVICE.getObjectType());
                    devopsEnvCommandE.setObjectId(devopsServiceE.getId());
                    devopsEnvCommandE.setCommandType(CommandType.CREATE.getCommandType());
                    devopsEnvCommandE.setStatus(CommandStatus.DOING.getCommandStatus());
                    devopsEnvCommandRepository.create(devopsEnvCommandE);
                }

                List<DevopsIngressPathE> devopsIngressPathEList = devopsIngressRepository.selectByEnvIdAndServiceName(
                        devopsServiceE.getEnvId(), devopsServiceE.getName());
                for (DevopsIngressPathE dd : devopsIngressPathEList) {
                    if (dd.getServiceId() == null) {
                        dd.setServiceId(devopsServiceE.getId());
                        devopsIngressRepository.updateIngressPath(dd);
                    }
                }
            }

            DevopsEnvResourceE newdevopsEnvResourceE = devopsEnvResourceRepository
                    .queryByInstanceIdAndKindAndName(
                            applicationInstanceE.getId(),
                            KeyParseTool.getResourceType(key),
                            KeyParseTool.getResourceName(key));
            saveOrUpdateResource(devopsEnvResourceE,
                    newdevopsEnvResourceE,
                    devopsEnvResourceDetailE,
                    applicationInstanceE);
        }
    }

    @Override
    public void helmReleaseGetContent(String key, Long envId, String msg) {
        ReleasePayload releasePayload = JSONArray.parseObject(msg, ReleasePayload.class);
        ApplicationInstanceE applicationInstanceE = helmRelease(envId, msg);
        if (applicationInstanceE != null) {
            List<Resource> resources = JSONArray.parseArray(releasePayload.getResources(), Resource.class);
            installResource(resources, applicationInstanceE);
        }
    }

    @Override
    public void commandNotSend(Long commandId, String msg) {
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(commandId);
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getCommandStatus());
        devopsEnvCommandE.setError(msg);
        devopsEnvCommandRepository.update(devopsEnvCommandE);
        if (devopsEnvCommandE.getCommandType().equals(CommandType.CREATE.getCommandType())) {
            if (devopsEnvCommandE.getObject().equals(ObjectType.INSTANCE.getObjectType())) {
                ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectById(devopsEnvCommandE.getObjectId());
                applicationInstanceE.setStatus(InstanceStatus.FAILED.getStatus());
                applicationInstanceRepository.update(applicationInstanceE);
            } else if (devopsEnvCommandE.getObject().equals(ObjectType.SERVICE.getObjectType())) {
                DevopsServiceE devopsServiceE = devopsServiceRepository.query(devopsEnvCommandE.getObjectId());
                devopsServiceE.setStatus(ServiceStatus.FAILED.getStatus());
                devopsServiceRepository.update(devopsServiceE);
            } else if (devopsEnvCommandE.getObject().equals(ObjectType.INGRESS.getObjectType())) {
                DevopsIngressDO ingress = devopsIngressRepository.getIngress(devopsEnvCommandE.getObjectId());
                ingress.setStatus(IngressStatus.FAILED.getStatus());
                devopsIngressRepository.updateIngress(ingress);
            }
        }
    }

    @Override
    public void resourceSync(String key, Long envId, String msg) {
        ResourceSyncPayload resourceSyncPayload = JSONArray.parseObject(msg, ResourceSyncPayload.class);
        ResourceType resourceType = ResourceType.forString(resourceSyncPayload.getResourceType());
        List<DevopsEnvResourceE> devopsEnvResourceES;
        if (resourceType != null) {
            switch (resourceType) {
                case POD:
                    devopsEnvResourceES = devopsEnvResourceRepository.listByEnvAndType(envId, ResourceType.POD.getType());
                    if (!devopsEnvResourceES.isEmpty()) {
                        List<String> podNames = Arrays.asList(resourceSyncPayload.getResources());
                        devopsEnvResourceES.parallelStream().filter(devopsEnvResourceE -> !podNames.contains(devopsEnvResourceE.getName())).forEach(devopsEnvResourceE -> {
                            devopsEnvResourceRepository.deleteByKindAndName(ResourceType.POD.getType(), devopsEnvResourceE.getName());
                            devopsEnvPodRepository.deleteByName(devopsEnvResourceE.getName(), KeyParseTool.getValue(key, "env"));
                        });
                    }
                    break;
                case DEPLOYMENT:
                    devopsEnvResourceES = devopsEnvResourceRepository.listByEnvAndType(envId, ResourceType.DEPLOYMENT.getType());
                    if (!devopsEnvResourceES.isEmpty()) {
                        List<String> deploymentNames = Arrays.asList(resourceSyncPayload.getResources());
                        devopsEnvResourceES.parallelStream().filter(devopsEnvResourceE -> !deploymentNames.contains(devopsEnvResourceE.getName())).forEach(devopsEnvResourceE ->
                                devopsEnvResourceRepository.deleteByKindAndName(ResourceType.DEPLOYMENT.getType(), devopsEnvResourceE.getName())
                        );
                    }
                    break;
                case REPLICASET:
                    devopsEnvResourceES = devopsEnvResourceRepository.listByEnvAndType(envId, ResourceType.REPLICASET.getType());
                    if (!devopsEnvResourceES.isEmpty()) {
                        List<String> replicaSetNames = Arrays.asList(resourceSyncPayload.getResources());
                        devopsEnvResourceES.parallelStream().filter(devopsEnvResourceE -> !replicaSetNames.contains(devopsEnvResourceE.getName())).forEach(devopsEnvResourceE ->
                                devopsEnvResourceRepository.deleteByKindAndName(ResourceType.REPLICASET.getType(), devopsEnvResourceE.getName())
                        );
                    }
                    break;
                case SERVICE:
                    List<DevopsServiceV> devopsServiceVS = devopsServiceRepository.listDevopsService(envId);
                    if (!devopsServiceVS.isEmpty()) {
                        List<String> seriviceNames = Arrays.asList(resourceSyncPayload.getResources());
                        devopsServiceVS.parallelStream().filter(devopsServiceV -> !seriviceNames.contains(devopsServiceV.getName())).forEach(devopsServiceV -> {
                            devopsServiceRepository.delete(devopsServiceV.getId());
                            devopsEnvResourceRepository.deleteByKindAndName(ResourceType.SERVICE.getType(), devopsServiceV.getName());
                        });
                    }
                    break;
                case INGRESS:
                    List<DevopsIngressE> devopsIngressES = devopsIngressRepository.listByEnvId(envId);
                    if (!devopsIngressES.isEmpty()) {
                        List<String> ingressNames = Arrays.asList(resourceSyncPayload.getResources());
                        devopsIngressES.parallelStream().filter(devopsIngressE -> !ingressNames.contains(devopsIngressE.getName())).forEach(devopsIngressE -> {
                            devopsIngressRepository.deleteIngress(devopsIngressE.getId());
                            devopsEnvResourceRepository.deleteByKindAndName(ResourceType.INGRESS.getType(), devopsIngressE.getName());
                        });
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void saveOrUpdateResource(DevopsEnvResourceE devopsEnvResourceE,
                                      DevopsEnvResourceE newdevopsEnvResourceE,
                                      DevopsEnvResourceDetailE devopsEnvResourceDetailE,
                                      ApplicationInstanceE applicationInstanceE) {
        if (newdevopsEnvResourceE == null) {
            devopsEnvResourceE.initDevopsInstanceResourceMessageE(
                    devopsEnvResourceDetailRepository.create(devopsEnvResourceDetailE).getId());
            if (!devopsEnvResourceE.getKind().equals(ResourceType.INGRESS.getType())) {
                devopsEnvResourceE.initApplicationInstanceE(applicationInstanceE.getId());
            }
            devopsEnvResourceRepository.create(devopsEnvResourceE);
            return;
        }
        if (newdevopsEnvResourceE.getReversion() == null) {
            newdevopsEnvResourceE.setReversion(0L);
        }
        if (devopsEnvResourceE.getReversion() == null) {
            devopsEnvResourceE.setReversion(0L);
        }
        if (!newdevopsEnvResourceE.getReversion().equals(devopsEnvResourceE.getReversion())) {
            newdevopsEnvResourceE.setReversion(devopsEnvResourceE.getReversion());
            devopsEnvResourceDetailE.setId(
                    newdevopsEnvResourceE.getDevopsEnvResourceDetailE().getId());
            devopsEnvResourceRepository.update(newdevopsEnvResourceE);
            devopsEnvResourceDetailRepository.update(devopsEnvResourceDetailE);
        }
    }

    public void installResource(List<Resource> resources, ApplicationInstanceE applicationInstanceE) {
        try {
            for (Resource resource : resources) {
                DevopsEnvResourceE newdevopsEnvResourceE =
                        devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                                applicationInstanceE.getId(),
                                resource.getKind(),
                                resource.getName());
                DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
                devopsEnvResourceDetailE.setMessage(resource.getObject());
                DevopsEnvResourceE devopsEnvResourceE =
                        DevopsInstanceResourceFactory.createDevopsInstanceResourceE();
                devopsEnvResourceE.setKind(resource.getKind());
                devopsEnvResourceE.setName(resource.getName());
                JSONObject jsonResult = JSONObject.parseObject(JSONObject.parseObject(resource.getObject()).get(METADATA).toString());
                devopsEnvResourceE.setReversion(
                        TypeUtil.objToLong(jsonResult.get(RESOURCEVERSION).toString()));
                saveOrUpdateResource(
                        devopsEnvResourceE,
                        newdevopsEnvResourceE,
                        devopsEnvResourceDetailE,
                        applicationInstanceE);
                if (resource.getKind().equals(ResourceType.POD.getType())) {
                    V1Pod v1Pod = json.deserialize(resource.getObject(), V1Pod.class);
                    String status = K8sUtil.changePodStatus(v1Pod);
                    String resourceVersion = v1Pod.getMetadata().getResourceVersion();

                    DevopsEnvPodE devopsEnvPodE = new DevopsEnvPodE();
                    devopsEnvPodE.setName(v1Pod.getMetadata().getName());
                    devopsEnvPodE.setIp(v1Pod.getStatus().getPodIP());
                    devopsEnvPodE.setStatus(status);
                    devopsEnvPodE.setResourceVersion(resourceVersion);
                    devopsEnvPodE.setNamespace(v1Pod.getMetadata().getNamespace());
                    if (!PENDING.equals(status)) {
                        devopsEnvPodE.setReady(v1Pod.getStatus().getContainerStatuses().get(0).isReady());
                    } else {
                        devopsEnvPodE.setReady(false);
                    }
                    devopsEnvPodE.initApplicationInstanceE(applicationInstanceE.getId());
                    devopsEnvPodRepository.insert(devopsEnvPodE);
                    Long podId = devopsEnvPodRepository.get(devopsEnvPodE).getId();
                    v1Pod.getSpec().getContainers().parallelStream().forEach(t ->
                            containerRepository.insert(new DevopsEnvPodContainerDO(
                                    podId,
                                    t.getName())));
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
}

