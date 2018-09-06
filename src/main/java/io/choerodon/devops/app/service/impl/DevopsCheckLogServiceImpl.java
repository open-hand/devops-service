package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.zaxxer.hikari.util.DefaultThreadFactory;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.application.valueobject.CheckLog;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.InstanceStatus;
import io.choerodon.devops.infra.common.util.enums.ResourceType;
import io.choerodon.devops.infra.common.util.enums.ServiceStatus;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.dataobject.DevopsProjectDO;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.mapper.ApplicationMapper;

@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {

    private static final Integer ADMIN = 1;
    private static final String ENV = "ENV";
    private static final String CREATE = "create";
    private static final String SERVICE_LABLE = "choerodon.io/network";
    private static final String SERVICE = "service";
    private static final String SUCCESS = "success";
    private static final String FAILED = "failed: ";
    private static final String SERIAL_STRING = " serializable to yaml";
    private static final String MASTER = "master";
    private static final String YAML_FILE = ".yaml";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);
    private static io.kubernetes.client.JSON json = new io.kubernetes.client.JSON();
    private Gson gson = new Gson();
    @Value("${services.gateway.url}")
    private String gatewayUrl;
    @Value("${services.helm.url}")
    private String helmUrl;


    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private DevopsCheckLogRepository devopsCheckLogRepository;
    @Autowired
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private DevopsServiceRepository devopsServiceRepository;
    @Autowired
    private DevopsIngressRepository devopsIngressRepository;
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private DevopsEnvResourceDetailRepository devopsEnvResourceDetailRepository;
    @Autowired
    private DevopsEnvResourceRepository devopsEnvResourceRepository;
    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;

    private static final ExecutorService executorService = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("devops-upgrade", false));

    @Override
    public void checkLog(String version) {
        LOGGER.info("start upgrade task");
        executorService.submit(new UpgradeTask(version));
    }

    @Override
    @Async
    public void updateUserMemberRole(String version) {
        DevopsCheckLogE devopsCheckLogE = new DevopsCheckLogE();
        List<CheckLog> logs = new ArrayList<>();
        devopsCheckLogE.setBeginCheckDate(new Date());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(" update start");
        }
        if ("0.9".equals(version)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(" update member role start");
            }
            gitOpsUserAccess();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(" sync env start");
            }
            syncEnvProject(logs);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(" sync object start");
            }
            syncObjects(logs);

        }
        devopsCheckLogE.setLog(JSON.toJSONString(logs));
        devopsCheckLogE.setEndCheckDate(new Date());
        devopsCheckLogRepository.create(devopsCheckLogE);
    }


    private void syncWebHook(ApplicationDO applicationDO, List<CheckLog> logs) {
        CheckLog checkLog = new CheckLog();
        checkLog.setContent("app: " + applicationDO.getName() + " create gitlab webhook");
        try {
            ProjectHook projectHook = ProjectHook.allHook();
            projectHook.setEnableSslVerification(true);
            projectHook.setProjectId(applicationDO.getGitlabProjectId());
            projectHook.setToken(applicationDO.getToken());
            String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
            uri += "devops/webhook";
            projectHook.setUrl(uri);
            applicationDO.setHookId(TypeUtil.objToLong(
                    gitlabRepository.createWebHook(applicationDO.getGitlabProjectId(), ADMIN, projectHook).getId()));
            applicationMapper.updateByPrimaryKey(applicationDO);
            checkLog.setResult(SUCCESS);
        } catch (Exception e) {
            checkLog.setResult(FAILED + e.getMessage());
        }
        logs.add(checkLog);
    }


    private void syncBranches(ApplicationDO applicationDO, List<CheckLog> logs) {
        CheckLog checkLog = new CheckLog();
        checkLog.setContent("app: " + applicationDO.getName() + " sync branches");
        try {
            Optional<List<BranchDO>> branchDOS = Optional.ofNullable(
                    gitlabServiceClient.listBranches(applicationDO.getGitlabProjectId(), ADMIN).getBody());
            List<String> branchNames =
                    devopsGitRepository.listDevopsBranchesByAppId(applicationDO.getId()).parallelStream()
                            .map(DevopsBranchE::getBranchName).collect(Collectors.toList());
            branchDOS.ifPresent(branchDOS1 -> branchDOS1.parallelStream()
                    .filter(branchDO -> !branchNames.contains(branchDO.getName()))
                    .forEach(branchDO -> {
                        DevopsBranchE newDevopsBranchE = new DevopsBranchE();
                        newDevopsBranchE.initApplicationE(applicationDO.getId());
                        newDevopsBranchE.setLastCommitDate(branchDO.getCommit().getCommittedDate());
                        newDevopsBranchE.setLastCommit(branchDO.getCommit().getId());
                        newDevopsBranchE.setBranchName(branchDO.getName());
                        newDevopsBranchE.setCheckoutCommit(branchDO.getCommit().getId());
                        newDevopsBranchE.setCheckoutDate(branchDO.getCommit().getCommittedDate());
                        newDevopsBranchE.setLastCommitMsg(branchDO.getCommit().getMessage());
                        UserE userE = iamRepository.queryByLoginName(branchDO.getCommit().getAuthorName());
                        newDevopsBranchE.setLastCommitUser(userE.getId());
                        devopsGitRepository.createDevopsBranch(newDevopsBranchE);
                        checkLog.setResult(SUCCESS);
                    }));
        } catch (Exception e) {
            checkLog.setResult(FAILED + e.getMessage());
        }
        logs.add(checkLog);
    }


    private void syncNonEnvGroupProject(List<CheckLog> logs) {
        List<DevopsProjectDO> projectDOList = devopsCheckLogRepository.queryNonEnvGroupProject();
        LOGGER.info("{} projects need to upgrade", projectDOList.size());
        final String groupCodeSuffix = "gitops";
        projectDOList.forEach(t -> {
            CheckLog checkLog = new CheckLog();
            try {
                Long projectId = t.getId();
                ProjectE projectE = iamRepository.queryIamProject(projectId);
                checkLog.setContent("project: " + projectE.getName() + " create gitops group");
                Organization organization = iamRepository
                        .queryOrganizationById(projectE.getOrganization().getId());
                //创建gitlab group
                GroupDO group = new GroupDO();
                // name: orgName-projectName
                group.setName(String.format("%s-%s-%s",
                        organization.getName(), projectE.getName(), groupCodeSuffix));
                // path: orgCode-projectCode
                group.setPath(String.format("%s-%s-%s",
                        organization.getCode(), projectE.getCode(), groupCodeSuffix));
                ResponseEntity<GroupDO> responseEntity = gitlabServiceClient.createGroup(group, ADMIN);
                if (responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
                    group = responseEntity.getBody();
                    DevopsProjectDO devopsProjectDO = new DevopsProjectDO(projectId);
                    devopsProjectDO.setEnvGroupId(group.getId());
                    devopsProjectRepository.updateProjectAttr(devopsProjectDO);
                    checkLog.setResult(SUCCESS);
                } else {
                    checkLog.setResult(FAILED + "create group response error! Header:"
                            + responseEntity.getHeaders() + "    Body: " + responseEntity.getBody().toString());
                }
            } catch (Exception e) {
                LOGGER.info("create project GitOps group error");
                checkLog.setResult(FAILED + e.getMessage());
            }
            LOGGER.info(checkLog.toString());
            logs.add(checkLog);
                });

    }


    private void syncEnvProject(List<CheckLog> logs) {
        LOGGER.info("start to sync env project");
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnvironmentRepository.list();
        devopsEnvironmentES
                .stream()
                .filter(devopsEnvironmentE -> devopsEnvironmentE.getGitlabEnvProjectId() == null)
                .forEach(devopsEnvironmentE -> {
                    CheckLog checkLog = new CheckLog();
                    try {
                        //generate git project code
                        checkLog.setContent("env: " + devopsEnvironmentE.getName() + " create gitops project");
                        ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
                        Organization organization = iamRepository
                                .queryOrganizationById(projectE.getOrganization().getId());
                        //generate rsa key
                        List<String> sshKeys = FileUtil.getSshKey(String.format("%s/%s/%s",
                                organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode()));
                        devopsEnvironmentE.setEnvIdRsa(sshKeys.get(0));
                        devopsEnvironmentE.setEnvIdRsaPub(sshKeys.get(1));
                        devopsEnvironmentRepository.update(devopsEnvironmentE);
                        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
                        GitlabGroupE gitlabGroupE = devopsProjectRepository.queryDevopsProject(projectE.getId());
                        gitlabProjectPayload.setGroupId(gitlabGroupE.getEnvGroupId());
                        gitlabProjectPayload.setUserId(ADMIN);
                        gitlabProjectPayload.setPath(devopsEnvironmentE.getCode());
                        gitlabProjectPayload.setOrganizationId(null);
                        gitlabProjectPayload.setType(ENV);
                        devopsEnvironmentService.handleCreateEnvSaga(gitlabProjectPayload);
                        checkLog.setResult(SUCCESS);
                    } catch (Exception e) {
                        LOGGER.info("create env git project error", e);
                        checkLog.setResult(FAILED + e.getMessage());
                    }
                    LOGGER.info(checkLog.toString());
                    logs.add(checkLog);
                });
    }


    private void syncObjects(List<CheckLog> logs) {
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnvironmentRepository.list();
        LOGGER.info("begin to sync env objects for {}  env", devopsEnvironmentES.size());
        devopsEnvironmentES.parallelStream().forEach(env -> {
            if (env.getGitlabEnvProjectId() != null) {
                LOGGER.info("{}:{}  begin to upgrade!", env.getCode(), env.getId());
                new SyncInstanceByEnv(logs, env).invoke();
                new SynServiceByEnv(logs, env).invoke();
                new SyncIngressByEnv(logs, env).invoke();
                devopsGitRepository.createTag(
                        TypeUtil.objToInteger(env.getGitlabEnvProjectId()), "agent-sync", MASTER, "", "", ADMIN);
                LOGGER.info("{}:{} finish to upgrade", env.getCode(), env.getId());
            }
        });
    }

    @Saga(code = "devops-upgrade-0.9",
            description = "devops smooth upgrade to 0.9", inputSchema = "{}")
    private void gitOpsUserAccess() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(" saga start");
        }
        sagaClient.startSaga("devops-upgrade-0.9", new StartInstanceDTO("{}", "", ""));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(" saga start success");
        }
    }

    public List<V1ServicePort> getServicePort(DevopsServiceE devopsServiceE) {
        final Integer[] serialNumber = {0};
        List<V1ServicePort> ports;
        if (devopsServiceE.getPorts() == null) {
            List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = devopsServiceInstanceRepository.queryByServiceId(devopsServiceE.getId());
            if (!devopsServiceAppInstanceES.isEmpty()) {
                DevopsEnvResourceE devopsEnvResourceE = devopsEnvResourceRepository.queryByInstanceIdAndKindAndName(devopsServiceAppInstanceES.get(0).getAppInstanceId(), ResourceType.SERVICE.getType(), devopsServiceE.getName());
                DevopsEnvResourceDetailE devopsEnvResourceDetailE = devopsEnvResourceDetailRepository.query(devopsEnvResourceE.getDevopsEnvResourceDetailE().getId());
                V1Service v1Service = json.deserialize(devopsEnvResourceDetailE.getMessage(),
                        V1Service.class);
                String port = TypeUtil.objToString(v1Service.getSpec().getPorts().get(0).getPort());
                if (port == null) {
                    port = "<none>";
                }
                String targetPort = TypeUtil.objToString(v1Service.getSpec().getPorts().get(0).getTargetPort());
                if (targetPort == null) {
                    targetPort = "<none>";
                }
                String name = v1Service.getSpec().getPorts().get(0).getName();
                String protocol = v1Service.getSpec().getPorts().get(0).getProtocol();
                List<PortMapE> portMapES = new ArrayList<>();
                PortMapE portMapE = new PortMapE();
                portMapE.setName(name);
                portMapE.setPort(TypeUtil.objToLong(port));
                portMapE.setProtocol(protocol);
                portMapE.setTargetPort(targetPort);
                portMapES.add(portMapE);
                ports = portMapES.parallelStream()
                        .map(t -> {
                            V1ServicePort v1ServicePort = new V1ServicePort();
                            if (t.getPort() != null) {
                                v1ServicePort.setPort(TypeUtil.objToInteger(t.getPort()));
                            }
                            if (t.getTargetPort() != null) {
                                v1ServicePort.setTargetPort(new IntOrString(t.getTargetPort()));
                            }
                            v1ServicePort.setName(t.getName());
                            v1ServicePort.setProtocol(t.getProtocol());
                            return v1ServicePort;
                        }).collect(Collectors.toList());
                devopsServiceE.setPorts(portMapES);
                devopsServiceRepository.update(devopsServiceE);
            } else {
                ports = null;
            }
        } else {
            ports = devopsServiceE.getPorts().parallelStream()
                    .map(t -> {
                        V1ServicePort v1ServicePort = new V1ServicePort();
                        if (t.getNodePort() != null) {
                            v1ServicePort.setNodePort(t.getNodePort().intValue());
                        }
                        if (t.getPort() != null) {
                            v1ServicePort.setPort(t.getPort().intValue());
                        }
                        if (t.getTargetPort() != null) {
                            v1ServicePort.setTargetPort(new IntOrString(t.getTargetPort()));
                        }
                        v1ServicePort.setName(t.getName() != null ? t.getName() : "http" + serialNumber[0]++);
                        v1ServicePort.setProtocol(t.getProtocol() != null ? t.getProtocol() : "TCP");
                        return v1ServicePort;
                    }).collect(Collectors.toList());
        }
        return ports;
    }

    private class SyncInstanceByEnv {
        private List<CheckLog> logs;
        private DevopsEnvironmentE env;

        SyncInstanceByEnv(List<CheckLog> logs, DevopsEnvironmentE env) {
            this.logs = logs;
            this.env = env;
        }

        void invoke() {
            applicationInstanceRepository.selectByEnvId(env.getId()).stream()
                    .filter(a -> !InstanceStatus.DELETED.getStatus().equals(a.getStatus()))
                    .forEach(applicationInstanceE -> {
                        CheckLog checkLog = new CheckLog();
                        try {
                            checkLog.setContent("instance: " + applicationInstanceE.getCode() + SERIAL_STRING);
                            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                                    .queryById(applicationInstanceE.getDevopsEnvironmentE().getId());
                            ObjectOperation<C7nHelmRelease> objectOperation = new ObjectOperation<>();
                            objectOperation.setType(getC7NHelmRelease(applicationInstanceE));
                            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
                            String filePath = "release-" + applicationInstanceE.getCode();
                            if (!gitlabRepository.getFile(projectId, MASTER, filePath + YAML_FILE)) {
                                objectOperation.operationEnvGitlabFile(
                                        filePath,
                                        projectId,
                                        CREATE,
                                        TypeUtil.objToLong(ADMIN), null, null, null, null);
                                checkLog.setResult(SUCCESS);
                            }
                            LOGGER.info(checkLog.toString());
                        } catch (Exception e) {
                            LOGGER.info("{}:{} instance/{} sync failed {}", env.getCode(), env.getId(), applicationInstanceE.getCode(), e);
                            checkLog.setResult(FAILED + e.getMessage());
                        }
                        logs.add(checkLog);
                    });
        }

        private C7nHelmRelease getC7NHelmRelease(ApplicationInstanceE applicationInstanceE) {
            ApplicationVersionE applicationVersionE = applicationVersionRepository
                    .query(applicationInstanceE.getApplicationVersionE().getId());
            ApplicationE applicationE = applicationRepository.query(applicationInstanceE.getApplicationE().getId());
            C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
            c7nHelmRelease.getMetadata().setName(applicationInstanceE.getCode());
            c7nHelmRelease.getSpec().setRepoUrl(helmUrl + applicationVersionE.getRepository());
            c7nHelmRelease.getSpec().setChartName(applicationE.getCode());
            c7nHelmRelease.getSpec().setChartVersion(applicationVersionE.getVersion());
            c7nHelmRelease.getSpec().setValues(applicationInstanceService.getReplaceResult(
                    applicationVersionRepository.queryValue(applicationVersionE.getId()),
                    applicationInstanceRepository.queryValueByEnvIdAndAppId(
                            applicationInstanceE.getDevopsEnvironmentE().getId(),
                            applicationE.getId())).getDeltaYaml().trim());
            return c7nHelmRelease;
        }
    }

    private class SynServiceByEnv {
        private List<CheckLog> logs;
        private DevopsEnvironmentE env;

        SynServiceByEnv(List<CheckLog> logs, DevopsEnvironmentE env) {
            this.logs = logs;
            this.env = env;
        }

        void invoke() {
            devopsServiceRepository.selectByEnvId(env.getId()).stream()
                    .filter(t -> !ServiceStatus.DELETED.getStatus().equals(t.getStatus()))
                    .forEach(devopsServiceE -> {
                        CheckLog checkLog = new CheckLog();
                        try {
                            checkLog.setContent("service: " + devopsServiceE.getName() + SERIAL_STRING);
                            V1Service service = getService(devopsServiceE);
                            DevopsEnvironmentE devopsEnvironmentE =
                                    devopsEnvironmentRepository.queryById(devopsServiceE.getEnvId());
                            ObjectOperation<V1Service> objectOperation = new ObjectOperation<>();
                            objectOperation.setType(service);
                            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
                            String filePath = "svc-" + devopsServiceE.getName();
                            if (!gitlabRepository.getFile(projectId, MASTER, filePath + YAML_FILE)) {
                                objectOperation.operationEnvGitlabFile(
                                        filePath,
                                        projectId,
                                        CREATE,
                                        TypeUtil.objToLong(ADMIN), null, null, null, null);
                                checkLog.setResult(SUCCESS);
                            }
                            LOGGER.info(checkLog.toString());
                        } catch (Exception e) {
                            LOGGER.info("{}:{} service/{} sync failed {}", env.getCode(), env.getId(), devopsServiceE.getName(), e);
                            checkLog.setResult(FAILED + e.getMessage());
                        }
                        logs.add(checkLog);
                    });
        }

        private V1Service getService(DevopsServiceE devopsServiceE) {
            V1Service service = new V1Service();
            service.setKind("Service");
            service.setApiVersion("v1");

            // metadata
            V1ObjectMeta metadata = new V1ObjectMeta();
            metadata.setName(devopsServiceE.getName());
            // metadata / labels
            Map<String, String> label = new HashMap<>();
            label.put(SERVICE_LABLE, SERVICE);
            metadata.setLabels(label);
            // metadata / annotations
            if (devopsServiceE.getAnnotations() != null) {
                Map<String, String> annotations = gson.fromJson(
                        devopsServiceE.getAnnotations(), new TypeToken<Map<String, String>>() {
                        }.getType());
                metadata.setAnnotations(annotations);
            }
            // set metadata
            service.setMetadata(metadata);

            V1ServiceSpec spec = new V1ServiceSpec();
            // spec / ports
            spec.setPorts(getServicePort(devopsServiceE));

            // spec / selector
            if (devopsServiceE.getLabels() != null) {
                Map<String, String> selector = gson.fromJson(
                        devopsServiceE.getLabels(), new TypeToken<Map<String, String>>() {
                        }.getType());
                spec.setSelector(selector);
            }

            // spec / externalIps
            if (!StringUtils.isEmpty(devopsServiceE.getExternalIp())) {
                List<String> externalIps = new ArrayList<>(Arrays.asList(devopsServiceE.getExternalIp().split(",")));
                spec.setExternalIPs(externalIps);
            }

            spec.setSessionAffinity("None");
            spec.type("ClusterIP");
            service.setSpec(spec);

            return service;
        }

    }

    private class SyncIngressByEnv {
        private List<CheckLog> logs;
        private DevopsEnvironmentE env;

        SyncIngressByEnv(List<CheckLog> logs, DevopsEnvironmentE env) {
            this.logs = logs;
            this.env = env;
        }

        void invoke() {
            devopsIngressRepository.listByEnvId(env.getId())
                    .forEach(devopsIngressE -> {
                        CheckLog checkLog = new CheckLog();
                        try {
                            checkLog.setContent("ingress: " + devopsIngressE.getName() + SERIAL_STRING);
                            DevopsEnvironmentE devopsEnvironmentE =
                                    devopsEnvironmentRepository.queryById(devopsIngressE.getEnvId());
                            ObjectOperation<V1beta1Ingress> objectOperation = new ObjectOperation<>();
                            objectOperation.setType(getV1beta1Ingress(devopsIngressE));
                            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
                            String filePath = "ing-" + devopsIngressE.getName();
                            if (!gitlabRepository.getFile(projectId, MASTER, filePath + YAML_FILE)) {
                                objectOperation.operationEnvGitlabFile(
                                        filePath,
                                        projectId,
                                        CREATE,
                                        TypeUtil.objToLong(ADMIN), null, null, null, null);
                                checkLog.setResult(SUCCESS);
                            }
                            LOGGER.info(checkLog.toString());
                        } catch (Exception e) {
                            LOGGER.info("{}:{} ingress/{} sync failed {}", env.getCode(), env.getId(), devopsIngressE.getName(), e);
                            checkLog.setResult(FAILED + e.getMessage());
                        }
                        logs.add(checkLog);
                    });
        }

        private V1beta1Ingress getV1beta1Ingress(DevopsIngressE devopsIngressE) {
            V1beta1Ingress v1beta1Ingress = devopsIngressService
                    .initV1beta1Ingress(devopsIngressE.getDomain(), devopsIngressE.getName(),   devopsIngressE.getCertName());
            List<DevopsIngressPathE> devopsIngressPathES =
                    devopsIngressRepository.selectByIngressId(devopsIngressE.getId());
            devopsIngressPathES.parallelStream()
                    .forEach(devopsIngressPathE ->
                            v1beta1Ingress.getSpec().getRules().get(0).getHttp()
                                    .addPathsItem(devopsIngressService.createPath(
                                            devopsIngressPathE.getPath(),
                                            devopsIngressPathE.getServiceId(),
                                            devopsIngressPathE.getServicePort())));

            return v1beta1Ingress;
        }
    }

    class UpgradeTask implements Runnable{
        private String version;

        UpgradeTask(String version){
            this.version = version;
        }
        @Override
        public void run() {
            DevopsCheckLogE devopsCheckLogE = new DevopsCheckLogE();
            List<CheckLog> logs = new ArrayList<>();
            devopsCheckLogE.setBeginCheckDate(new Date());
            if ("0.8".equals(version)) {
                LOGGER.info("Start to execute upgrade task 0.8");
                List<ApplicationDO> applications = applicationMapper.selectAll();
                applications.parallelStream()
                        .filter(applicationDO ->
                                applicationDO.getGitlabProjectId() != null && applicationDO.getHookId() == null)
                        .forEach(applicationDO -> {
                            syncWebHook(applicationDO, logs);
                            syncBranches(applicationDO, logs);
                        });
            } else if ("0.9".equals(version)) {
                LOGGER.info("Start to execute upgrade task 0.9");
                syncNonEnvGroupProject(logs);
                gitOpsUserAccess();
                syncEnvProject(logs);
                syncObjects(logs);
            } else {
                LOGGER.info("version not matched");
            }
            devopsCheckLogE.setLog(JSON.toJSONString(logs));
            devopsCheckLogE.setEndCheckDate(new Date());
            devopsCheckLogRepository.create(devopsCheckLogE);
        }
    }
}
