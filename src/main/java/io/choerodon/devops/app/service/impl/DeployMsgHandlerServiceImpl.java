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
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.*;
import io.choerodon.devops.infra.common.util.*;
import io.choerodon.devops.infra.common.util.enums.*;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodContainerDO;
import io.choerodon.devops.infra.dataobject.DevopsIngressDO;
import io.choerodon.devops.infra.mapper.ApplicationMarketMapper;
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
    private static final String SERVICE_KIND = "service";
    private static final String INGRESS_KIND = "ingress";
    private static final String INSTANCE_KIND = "instance";
    private static final String C7NHELMRELEASE_KIND = "c7nhelmrelease";
    private static final String CERTIFICATE_KIND = "certificate";
    private static final String PUBLIC = "public";
    private static final Logger logger = LoggerFactory.getLogger(DeployMsgHandlerServiceImpl.class);
    private static final String RESOURCE_VERSION = "resourceVersion";

    private static JSON json = new JSON();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private ObjectMapper mapper = new ObjectMapper();
    private Gson gson = new Gson();

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
    @Autowired
    private ApplicationMarketMapper applicationMarketMapper;
    @Autowired
    private ApplicationMarketRepository applicationMarketRepository;
    @Autowired
    private DevopsCommandEventRepository devopsCommandEventRepository;
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

    /**
     * pod 更新
     *
     * @param key   消息key
     * @param envId 环境Id
     * @param msg   消息msg
     */
    public void handlerUpdatePodMessage(String key, Long envId, String msg) {
        V1Pod v1Pod = json.deserialize(msg, V1Pod.class);
        ApplicationInstanceE applicationInstanceE =
                applicationInstanceRepository.selectByCode(KeyParseTool.getReleaseName(key), envId);
        if (applicationInstanceE == null) {
            logger.info("instance not found");
            return;
        }
        DevopsEnvResourceE devopsEnvResourceE = new DevopsEnvResourceE();
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
            handleEnvPod(v1Pod, applicationInstanceE, resourceVersion, devopsEnvPodE, flag, devopsEnvPodEList);
        }
    }

    private void handleEnvPod(V1Pod v1Pod, ApplicationInstanceE applicationInstanceE, String resourceVersion, DevopsEnvPodE devopsEnvPodE, Boolean flag, List<DevopsEnvPodE> devopsEnvPodEList) {
        if (devopsEnvPodEList == null || devopsEnvPodEList.isEmpty()) {
            devopsEnvPodE.initApplicationInstanceE(applicationInstanceE.getId());
            devopsEnvPodRepository.insert(devopsEnvPodE);
            Long podId = devopsEnvPodRepository.get(devopsEnvPodE).getId();
            v1Pod.getSpec().getContainers().stream().forEach(t ->
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
                        v1Pod.getSpec().getContainers().stream().forEach(t ->
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
                v1Pod.getSpec().getContainers().stream().forEach(t ->
                        containerRepository.insert(new DevopsEnvPodContainerDO(
                                podId,
                                t.getName())));
            }
        }
    }

    @Override
    public void handlerReleaseInstall(String msg, Long envId) {
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
                applicationInstanceE.setStatus(InstanceStatus.RUNNING.getStatus());
                applicationInstanceRepository.update(applicationInstanceE);
                installResource(resources, applicationInstanceE);
            }
        }
    }


    @Override
    public void handlerPreInstall(String msg, Long envId, String type) {
        if (msg.equals("null")) {
            return;
        }
        List<Job> jobs = JSONArray.parseArray(msg, Job.class);
        ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE();
        try {
            for (Job job : jobs) {
                applicationInstanceE = applicationInstanceRepository
                        .selectByCode(job.getReleaseName(), envId);
                DevopsEnvResourceE newdevopsEnvResourceE =
                        devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                                applicationInstanceE.getId(),
                                job.getKind(),
                                job.getName());
                DevopsEnvResourceE devopsEnvResourceE =
                        new DevopsEnvResourceE();
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
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .query(applicationInstanceE.getCommandId());
            devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
            devopsEnvCommandRepository.update(devopsEnvCommandE);
        } catch (Exception e) {
            throw new CommonException("error.resource.insert", e);
        }
    }

    @Override
    public void resourceUpdate(String key, Long envId, String msg) {
        try {
            Object obj = objectMapper.readValue(msg, Object.class);
            DevopsEnvResourceE devopsEnvResourceE =
                    new DevopsEnvResourceE();
            DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
            devopsEnvResourceDetailE.setMessage(msg);
            devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
            devopsEnvResourceE.setName(
                    KeyParseTool.getResourceName(key));
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
                            devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                                    null,
                                    KeyParseTool.getResourceType(key),
                                    KeyParseTool.getResourceName(key));
                    saveOrUpdateResource(devopsEnvResourceE, newdevopsEnvResourceE,
                            devopsEnvResourceDetailE, null);
                    break;
                case POD:
                    handlerUpdatePodMessage(key, envId, msg);
                    break;
                case SERVICE:
                    handleUpdateServiceMsg(key, envId, msg, devopsEnvResourceE);
                    break;
                default:
                    releaseName = KeyParseTool.getReleaseName(key);
                    applicationInstanceE = applicationInstanceRepository.selectByCode(releaseName, envId);
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

    private void handleUpdateServiceMsg(String key, Long envId, String msg, DevopsEnvResourceE devopsEnvResourceE) {
        ApplicationInstanceE applicationInstanceE;
        V1Service v1Service = json.deserialize(msg, V1Service.class);
        if (v1Service.getMetadata().getAnnotations() != null) {
            String releaseNames = v1Service.getMetadata().getAnnotations()
                    .get("choerodon.io/network-service-instances");
            if (releaseNames != null) {
                String[] releases = releaseNames.split("\\+");
                List<Long> beforeInstanceIdS = devopsEnvResourceRepository.listByEnvAndType(envId, SERVICE_KIND).stream().filter(devopsEnvResourceE1 -> devopsEnvResourceE1.getName().equals(v1Service.getMetadata().getName())).map(devopsEnvResourceE1 ->
                        devopsEnvResourceE1.getApplicationInstanceE().getId()
                ).collect(Collectors.toList());
                List<Long> afterInstanceIds = new ArrayList<>();
                for (String release : releases) {
                    applicationInstanceE = applicationInstanceRepository
                            .selectByCode(release, envId);
                    DevopsEnvResourceE newdevopsInsResourceE =
                            devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                                    applicationInstanceE.getId(),
                                    KeyParseTool.getResourceType(key),
                                    KeyParseTool.getResourceName(key));
                    DevopsEnvResourceDetailE newDevopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
                    newDevopsEnvResourceDetailE.setMessage(msg);
                    saveOrUpdateResource(devopsEnvResourceE, newdevopsInsResourceE,
                            newDevopsEnvResourceDetailE, applicationInstanceE);
                    afterInstanceIds.add(applicationInstanceE.getId());
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

    @Override
    public void resourceDelete(Long envId, String msg) {
        if (KeyParseTool.getResourceType(msg).equals(ResourceType.JOB.getType())) {
            return;
        }
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
                    devopsServiceRepository.selectByNameAndEnvId(
                            KeyParseTool.getResourceName(msg),
                            envId);
            //更新网络数据
            if (devopsServiceE != null) {
                DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
                devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
                devopsEnvCommandE.setObjectId(devopsServiceE.getId());
                devopsEnvCommandE.setObject(SERVICE_KIND);
                devopsEnvCommandRepository.create(devopsEnvCommandE);
                devopsServiceRepository.delete(devopsServiceE.getId());
            }
        }
        if (KeyParseTool.getResourceType(msg).equals(ResourceType.INGRESS.getType())) {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
            DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(
                    devopsEnvironmentE.getId(), KeyParseTool.getResourceName(msg));
            if (devopsIngressE != null) {
                DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
                devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
                devopsEnvCommandE.setObjectId(devopsIngressE.getId());
                devopsEnvCommandE.setObject(INGRESS_KIND);
                devopsEnvCommandRepository.create(devopsEnvCommandE);
                devopsIngressRepository.deleteIngress(devopsIngressE.getId());
                devopsIngressRepository.deleteIngressPath(devopsIngressE.getId());
            }
        }

        devopsEnvResourceRepository.deleteByKindAndName(
                KeyParseTool.getResourceType(msg),
                KeyParseTool.getResourceName(msg));
    }

    @Override
    public void helmReleaseHookLogs(String key, String msg, Long envId) {
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                .selectByCode(KeyParseTool.getReleaseName(key), envId);
        if (applicationInstanceE != null) {
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
    public void updateInstanceStatus(String key, Long envId, String instanceStatus, String commandStatus, String msg) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectByCode(key, envId);
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
    public void helmReleaseDelete(String key, Long envId) {
        ApplicationInstanceE instanceE = applicationInstanceRepository.selectByCode(key, envId);
        if (instanceE != null) {
            DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
            devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
            devopsEnvCommandE.setCommandType(CommandType.DELETE.getType());
            devopsEnvCommandE.setObjectId(instanceE.getId());
            devopsEnvCommandE.setObject(INSTANCE_KIND);
            devopsEnvCommandRepository.create(devopsEnvCommandE);
            applicationInstanceRepository.deleteById(instanceE.getId());
        }
    }

    @Override
    public void handlerDomainCreateMessage(String key, String msg, Long envId) {
        V1beta1Ingress ingress = json.deserialize(msg, V1beta1Ingress.class);
        DevopsEnvResourceE devopsEnvResourceE = new DevopsEnvResourceE();
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
        devopsIngressRepository.setStatus(envId, ingressName, IngressStatus.RUNNING.getStatus());
    }

    @Override
    public void helmReleasePreUpgrade(String msg, Long envId, String type) {
        handlerPreInstall(msg, envId, type);
    }

    @Override
    public void handlerReleaseUpgrade(String msg, Long envId) {
        handlerReleaseInstall(msg, envId);
    }

    private ApplicationInstanceE helmRelease(Long envId, String msg) {
        ReleasePayload releasePayload = JSONArray.parseObject(msg, ReleasePayload.class);
        ApplicationVersionValueE applicationVersionValueE = new ApplicationVersionValueE();
        ApplicationVersionE applicationVersionE = new ApplicationVersionE();
        if (applicationInstanceRepository.selectByCode(releasePayload.getName(), envId) == null) {
            try {
                ApplicationInstanceE applicationInstanceE = new ApplicationInstanceE();
                DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                        .queryById(envId);
                ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
                Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
                getApplication(releasePayload.getChartName(), devopsEnvironmentE.getProjectE().getId(), organization.getId());
                ApplicationE applicationE = getApplication(releasePayload.getChartName(), devopsEnvironmentE.getProjectE().getId(), organization.getId());
                if (applicationE == null) {
                    throw new CommonException("error.application.query");
                }
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
                DevopsEnvCommandValueE devopsEnvCommandValueE = new DevopsEnvCommandValueE();
                devopsEnvCommandValueE.setValue(releasePayload.getConfig());
                devopsEnvCommandE.setObject(ObjectType.INSTANCE.getType());
                devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
                devopsEnvCommandE.setObjectId(applicationInstanceRepository.create(applicationInstanceE).getId());
                devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsEnvCommandE.initDevopsEnvCommandValueE(
                        devopsEnvCommandValueRepository.create(devopsEnvCommandValueE).getId());
                devopsEnvCommandRepository.create(devopsEnvCommandE);
                applicationInstanceE.setId(devopsEnvCommandE.getObjectId());
                return applicationInstanceE;
            } catch (Exception e) {
                throw new CommonException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public void helmReleaseDeleteFail(String key, String msg, Long envId) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                envId,
                InstanceStatus.DELETED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaseStartFail(String key, String msg, Long envId) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                envId,
                InstanceStatus.STOPPED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaseRollBackFail(String key, String msg) {
        logger.info(key);
    }

    @Override
    public void helmReleaseInstallFail(String key, String msg, Long envId) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                envId,
                InstanceStatus.FAILED.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaseUpgradeFail(String key, String msg, Long envId) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                envId,
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);
    }

    @Override
    public void helmReleaeStopFail(String key, String msg, Long envId) {
        updateInstanceStatus(KeyParseTool.getReleaseName(key),
                envId,
                InstanceStatus.RUNNING.getStatus(),
                CommandStatus.FAILED.getStatus(),
                msg);

    }

    @Override
    public void netWorkServiceFail(String key, Long envId, String msg) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                KeyParseTool.getValue(key, SERVICE_KIND), envId);
        devopsServiceE.setStatus(ServiceStatus.FAILED.getStatus());
        devopsServiceRepository.update(devopsServiceE);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.SERVICE.getType(), devopsServiceE.getId());
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getStatus());
        devopsEnvCommandE.setError(msg);
        devopsEnvCommandRepository.update(devopsEnvCommandE);
    }

    @Override
    public void netWorkIngressFail(String key, Long envId, String msg) {
        DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(
                envId, KeyParseTool.getValue(key, INGRESS_KIND));
        devopsIngressE.setStatus(IngressStatus.FAILED.getStatus());
        devopsIngressRepository.updateIngress(ConvertHelper.convert(devopsIngressE, DevopsIngressDO.class));
    }

    @Override
    public void netWorkServiceDeleteFail(String key, Long envId, String msg) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                KeyParseTool.getValue(key, SERVICE_KIND), envId);
        devopsServiceE.setStatus(ServiceStatus.DELETED.getStatus());
        devopsServiceRepository.update(devopsServiceE);
        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                .queryByObject(ObjectType.SERVICE.getType(), devopsServiceE.getId());
        devopsEnvCommandE.setStatus(CommandStatus.FAILED.getStatus());
        devopsEnvCommandE.setError(msg);
        devopsEnvCommandRepository.update(devopsEnvCommandE);
    }

    @Override
    public void netWorkIngressDeleteFail(String key, Long envId, String msg) {
        DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(
                envId, KeyParseTool.getValue(key, INGRESS_KIND));
        devopsIngressRepository.deleteIngress(devopsIngressE.getId());
    }

    @Override
    public void netWorkUpdate(String key, String msg, Long envId) {
        V1Service v1Service = json.deserialize(msg, V1Service.class);

        String releaseNames = v1Service.getMetadata().getAnnotations().get("choerodon.io/network-service-instances");
        String[] releases = releaseNames.split("\\+");

        DevopsEnvResourceE devopsEnvResourceE =
                new DevopsEnvResourceE();
        DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
        devopsEnvResourceDetailE.setMessage(msg);
        devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
        devopsEnvResourceE.setName(
                KeyParseTool.getResourceName(key));
        devopsEnvResourceE.setReversion(
                TypeUtil.objToLong(v1Service.getMetadata().getResourceVersion()));

        Boolean flag = false;
        Map<String, String> label = v1Service.getMetadata().getLabels();
        if (label.get(SERVICE_LABLE) != null && label.get(SERVICE_LABLE).equals(SERVICE_KIND)) {
            flag = true;
        }
        for (String release : releases) {
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                    .selectByCode(release, envId);

            if (flag) {
                DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(
                        v1Service.getMetadata().getName(), envId);
                if (devopsServiceE == null) {
                    devopsServiceE = new DevopsServiceE();
                    syncService(devopsServiceE, msg, applicationInstanceE);
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
    public void resourceSync(String key, Long envId, String msg) {
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
                                devopsEnvResourceRepository.deleteByKindAndName(
                                        ResourceType.POD.getType(), devopsEnvResourceE.getName());
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
                                    devopsEnvResourceRepository.deleteByKindAndName(
                                            ResourceType.DEPLOYMENT.getType(), devopsEnvResourceE.getName()));
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
                                    devopsEnvResourceRepository.deleteByKindAndName(
                                            ResourceType.REPLICASET.getType(), devopsEnvResourceE.getName()));
                }
                break;
            case SERVICE:
                List<DevopsServiceV> devopsServiceVS = devopsServiceRepository.listDevopsService(envId);
                if (!devopsServiceVS.isEmpty()) {
                    List<String> seriviceNames = Arrays.asList(resourceSyncPayload.getResources());
                    devopsServiceVS.stream()
                            .filter(devopsServiceV -> !seriviceNames.contains(devopsServiceV.getName()))
                            .forEach(devopsServiceV -> {
                                devopsServiceRepository.delete(devopsServiceV.getId());
                                devopsEnvResourceRepository.deleteByKindAndName(
                                        ResourceType.SERVICE.getType(), devopsServiceV.getName());
                            });
                }
                break;
            case INGRESS:
                List<DevopsIngressE> devopsIngressES = devopsIngressRepository.listByEnvId(envId);
                if (!devopsIngressES.isEmpty()) {
                    List<String> ingressNames = Arrays.asList(resourceSyncPayload.getResources());
                    devopsIngressES.stream()
                            .filter(devopsIngressE -> !ingressNames.contains(devopsIngressE.getName()))
                            .forEach(devopsIngressE -> {
                                devopsIngressRepository.deleteIngress(devopsIngressE.getId());
                                devopsEnvResourceRepository.deleteByKindAndName(
                                        ResourceType.INGRESS.getType(), devopsIngressE.getName());
                            });
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void jobEvent(String key, String msg, Long envId) {
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
    public void releasePodEvent(String key, String msg, Long envId) {
        Event event = JSONArray.parseObject(msg, Event.class);
        insertDevopsCommandEvent(event, ResourceType.POD.getType());
    }

    @Override
    public void gitOpsSyncEvent(Long envId, String msg) {
        logger.info("env {} receive git ops msg :\n{}", envId, msg);
        GitOpsSync gitOpsSync = JSONArray.parseObject(msg, GitOpsSync.class);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        DevopsEnvCommitE agentSyncCommit = devopsEnvCommitRepository.query(devopsEnvironmentE.getAgentSyncCommit());
        if (agentSyncCommit != null && agentSyncCommit.getCommitSha().equals(gitOpsSync.getMetadata().getCommit())) {
            return;
        }
        devopsEnvironmentE.setAgentSyncCommit(devopsEnvCommitRepository.queryByEnvIdAndCommit(envId, gitOpsSync.getMetadata().getCommit()).getId());
        devopsEnvironmentRepository.updateEnvCommit(devopsEnvironmentE);
        if (gitOpsSync.getResourceIDs() == null) {
            return;
        }
        if (gitOpsSync.getResourceIDs().isEmpty()) {
            return;
        }
        List<DevopsEnvFileErrorE> errorDevopsFiles = getEnvFileErrors(envId, gitOpsSync, devopsEnvironmentE);

        gitOpsSync.getMetadata().getFilesCommit().stream().forEach(fileCommit -> {
            DevopsEnvFileE devopsEnvFileE = devopsEnvFileRepository.queryByEnvAndPath(devopsEnvironmentE.getId(), fileCommit.getFile());
            devopsEnvFileE.setAgentCommit(fileCommit.getCommit());
            devopsEnvFileRepository.update(devopsEnvFileE);
        });
        gitOpsSync.getMetadata().getResourceCommits()
                .stream()
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
                            syncService(envId, devopsEnvironmentE, errorDevopsFiles, resourceCommit, objects);
                            break;
                        case CERTIFICATE_KIND:
                            syncCetificate(envId, errorDevopsFiles, resourceCommit, objects);
                            break;
                        default:
                            break;
                    }
                });
    }

    private void syncCetificate(Long envId, List<DevopsEnvFileErrorE> errorDevopsFiles, ResourceCommit resourceCommit, String[] objects) {
        DevopsEnvFileResourceE devopsEnvFileResourceE;
        CertificationE certificationE = certificationRepository
                .queryByEnvAndName(envId, objects[1]);
        devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(
                        envId, certificationE.getId(), ObjectType.CERTIFICATE.getType());
        if (updateEnvCommandStatus(resourceCommit, certificationE.getCommandId(),
                devopsEnvFileResourceE, CERTIFICATE_KIND, certificationE.getName(),
                null, errorDevopsFiles)) {
            certificationE.setStatus(CertificationStatus.FAILED.getStatus());
        } else {
            certificationE.setStatus(CertificationStatus.APPLYING.getStatus());
        }
        certificationRepository.updateStatus(certificationE);
    }

    private void syncService(Long envId, DevopsEnvironmentE devopsEnvironmentE, List<DevopsEnvFileErrorE> errorDevopsFiles, ResourceCommit resourceCommit, String[] objects) {
        DevopsEnvFileResourceE devopsEnvFileResourceE;
        DevopsServiceE devopsServiceE = devopsServiceRepository
                .selectByNameAndEnvId(objects[1], envId);
        devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(envId, devopsServiceE.getId(), SERVICE_KIND);
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
                .queryByEnvIdAndResource(envId, devopsIngressE.getId(), INGRESS_KIND);
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
                .queryByEnvIdAndResource(envId, applicationInstanceE.getId(), C7NHELMRELEASE_KIND);
        if (updateEnvCommandStatus(resourceCommit, applicationInstanceE.getCommandId(),
                devopsEnvFileResourceE, C7NHELMRELEASE_KIND, applicationInstanceE.getCode(), null, errorDevopsFiles)) {
            applicationInstanceE.setStatus(InstanceStatus.FAILED.getStatus());
            applicationInstanceRepository.update(applicationInstanceE);
        }
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
        if (devopsEnvResourceE.getApplicationInstanceE() != null) {
            newdevopsEnvResourceE.initApplicationInstanceE(devopsEnvResourceE.getApplicationInstanceE().getId());
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
                        devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(
                                instanceId,
                                resource.getKind(),
                                resource.getName());
                DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
                devopsEnvResourceDetailE.setMessage(resource.getObject());
                DevopsEnvResourceE devopsEnvResourceE =
                        new DevopsEnvResourceE();
                devopsEnvResourceE.setKind(resource.getKind());
                devopsEnvResourceE.setName(resource.getName());
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
            devopsEnvResourceRepository.deleteByKindAndName(
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
        if (!PENDING.equals(status)) {
            devopsEnvPodE.setReady(v1Pod.getStatus().getContainerStatuses().get(0).isReady());
        } else {
            devopsEnvPodE.setReady(false);
        }
        devopsEnvPodE.initApplicationInstanceE(applicationInstanceE.getId());
        devopsEnvPodRepository.insert(devopsEnvPodE);
        Long podId = devopsEnvPodRepository.get(devopsEnvPodE).getId();
        v1Pod.getSpec().getContainers().stream().forEach(t ->
                containerRepository.insert(new DevopsEnvPodContainerDO(
                        podId,
                        t.getName())));
    }

    private void syncIngress(String msg, Long envId) {
        V1beta1Ingress v1beta1Ingress = json.deserialize(msg, V1beta1Ingress.class);
        Map<String, String> label = v1beta1Ingress.getMetadata().getLabels();
        if (label.get(SERVICE_LABLE) != null
                && label.get(SERVICE_LABLE).equals(INGRESS_KIND)) {
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
                DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
                devopsEnvCommandE.setObject(ObjectType.INGRESS.getType());
                devopsEnvCommandE.setObjectId(devopsIngressE.getId());
                devopsEnvCommandE.setCommandType(CommandType.CREATE.getType());
                devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
                devopsEnvCommandRepository.create(devopsEnvCommandE);
                List<V1beta1HTTPIngressPath> paths = v1beta1Ingress.getSpec().getRules()
                        .get(0).getHttp().getPaths();
                for (V1beta1HTTPIngressPath path : paths) {
                    DevopsIngressPathE devopsIngressPathE = new DevopsIngressPathE();
                    devopsIngressPathE.setDevopsIngressE(devopsIngressE);
                    devopsIngressPathE.setServiceName(path.getBackend().getServiceName());
                    DevopsServiceE devopsServiceE = devopsServiceRepository
                            .selectByNameAndEnvId(path.getBackend().getServiceName(),
                                    envId);
                    if (devopsServiceE != null) {
                        devopsIngressPathE.setServiceId(devopsServiceE.getId());
                    }
                    devopsIngressPathE.setPath(path.getPath());
                    devopsIngressRepository.insertIngressPath(devopsIngressPathE);
                }
            }
        } else {
            devopsEnvResourceRepository.deleteByKindAndName(
                    ResourceType.INGRESS.getType(), v1beta1Ingress.getMetadata().getName());
        }
    }

    private void insertDevopsCommandEvent(Event event, String type) {
        DevopsEnvResourceE devopsEnvResourceE = devopsEnvResourceRepository
                .queryLatestJob(event.getInvolvedObject().getKind(), event.getInvolvedObject().getName());
        try {
            DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.INSTANCE.getType(), devopsEnvResourceE.getApplicationInstanceE().getId());
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
    public ApplicationE getApplication(String appName, Long projectId, Long orgId) {
        ApplicationE applicationE = applicationRepository
                .queryByCode(appName, projectId);
        if (applicationE == null) {
            List<ApplicationE> applicationES = applicationRepository.listByCode(appName);
            List<ApplicationE> applicationList = applicationES.stream()
                    .filter(newApplicationE ->
                            iamRepository.queryIamProject(newApplicationE.getProjectE().getId())
                                    .getOrganization().getId().equals(orgId))
                    .collect(Collectors.toList());
            applicationE = findAppInAppMarket(applicationES, applicationList);
        }
        return applicationE;
    }

    private ApplicationE findAppInAppMarket(List<ApplicationE> applicationES, List<ApplicationE> applicationList) {
        ApplicationE applicationE;
        if (!applicationList.isEmpty()) {
            applicationE = applicationList.get(0);
            if (applicationMarketMapper.selectCountByAppId(applicationE.getId()) == 0) {
                applicationE = null;
            }
        } else {
            applicationE = applicationES.isEmpty() ? null : applicationES.get(0);
            if (applicationE != null) {
                ApplicationMarketE applicationMarketE =
                        applicationMarketRepository.queryByAppId(applicationE.getId());
                if (applicationMarketE == null
                        || !applicationMarketE.getPublishLevel().equals(PUBLIC)) {
                    applicationE = null;
                }
            }
        }
        return applicationE;
    }

    @Override
    public void gitOpsCommandSyncEvent(Long envId) {
        logger.info("sync command status!");
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        List<Command> commands = new ArrayList<>();
        getCommands(envId, commands);
        Msg msg = new Msg();
        msg.setKey(String.format("env:%s.envId:%d",
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
    public void gitOpsCommandSyncEventResult(Long envId, String msg) {
        logger.info("sync command status result: {}.", msg);
        List<Command> commands = JSONArray.parseArray(msg, Command.class);
        List<Command> oldCommands = new ArrayList<>();
        getCommands(envId, oldCommands);
        if (!oldCommands.isEmpty()) {
            oldCommands.forEach(command ->
                    commands.stream().filter(command1 -> command1.getId().equals(command.getId())).forEach(command1 -> {
                        DevopsEnvCommandE devopsEnvCommandE = devopsEnvCommandRepository.query(command.getId());
                        if (command1.getCommit() != null && command.getCommit().equals(command1.getCommit())) {
                            devopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getStatus());
                            updateResourceStatus(envId, devopsEnvCommandE, InstanceStatus.RUNNING, ServiceStatus.RUNNING, IngressStatus.RUNNING, CertificationStatus.ACTIVE);
                        } else {
                            devopsEnvCommandE.setStatus(CommandStatus.FAILED.getStatus());
                            updateResourceStatus(envId, devopsEnvCommandE, InstanceStatus.FAILED, ServiceStatus.FAILED, IngressStatus.FAILED, CertificationStatus.FAILED);
                        }
                        devopsEnvCommandRepository.update(devopsEnvCommandE);
                    })
            );
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
    public void certIssued(String key, Long envId, String msg) {
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
    public void certFailed(String key, Long envId, String msg) {
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
}

