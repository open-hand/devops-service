package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import org.apache.commons.lang.StringUtils;
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

    @Override
    @Async
    public void checkLog(String version) {
        DevopsCheckLogE devopsCheckLogE = new DevopsCheckLogE();
        List<CheckLog> logs = new ArrayList<>();
        devopsCheckLogE.setBeginCheckDate(new Date());
        switch (version) {
            case "0.8":
                List<ApplicationDO> applications = applicationMapper.selectAll();
                applications.parallelStream()
                        .filter(applicationDO ->
                                applicationDO.getGitlabProjectId() != null && applicationDO.getHookId() == null)
                        .forEach(applicationDO -> {
                            syncWebHook(applicationDO, logs);
                            syncBranches(applicationDO, logs);
                        });
                break;
            case "0.9":
                syncNonEnvGroupProject(logs);
                gitOpsUserAccess();
                syncEnvProject(logs);
                syncObjects(logs);
                break;
            default:
                break;
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
        final String groupCodeSuffix = "gitops";
        projectDOList.parallelStream()
                .forEach(t -> {
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
                        }
                        checkLog.setResult(SUCCESS);
                    } catch (Exception e) {
                        checkLog.setResult(FAILED + e.getMessage());
                    }
                    logs.add(checkLog);
                });

    }


    private void syncEnvProject(List<CheckLog> logs) {
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnvironmentRepository.list();
        devopsEnvironmentES.parallelStream()
                .filter(devopsEnvironmentE -> devopsEnvironmentE.getGitlabEnvProjectId() == null)
                .forEach(devopsEnvironmentE -> {
                    CheckLog checkLog = new CheckLog();
                    try {
                        checkLog.setContent("env: " + devopsEnvironmentE.getName() + " create gitops project");
                        ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
                        Organization organization = iamRepository
                                .queryOrganizationById(projectE.getOrganization().getId());
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
                        checkLog.setResult(FAILED + e.getMessage());
                    }
                    logs.add(checkLog);
                });
    }


    private void syncObjects(List<CheckLog> logs) {
        List<ApplicationInstanceE> applicationInstanceES = applicationInstanceRepository.list();
        String serialString = " serializable to yaml";
        String master = "master";
        String yamlFile = ".yaml";
        applicationInstanceES.parallelStream()
                .filter(t -> !InstanceStatus.DELETED.getStatus().equals(t.getStatus()))
                .forEach(applicationInstanceE -> {
                    CheckLog checkLog = new CheckLog();
                    try {
                        checkLog.setContent("instance: " + applicationInstanceE.getCode() + serialString);
                        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                                .queryById(applicationInstanceE.getDevopsEnvironmentE().getId());
                        ObjectOperation<C7nHelmRelease> objectOperation = new ObjectOperation<>();
                        objectOperation.setType(getC7NHelmRelease(applicationInstanceE));
                        Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
                        String filePath = "release-" + applicationInstanceE.getCode();
                        if (!gitlabRepository.getFile(projectId, master, filePath + yamlFile)) {
                            objectOperation.operationEnvGitlabFile(
                                    filePath,
                                    projectId,
                                    CREATE,
                                    TypeUtil.objToLong(ADMIN), null, null, null, null);
                            checkLog.setResult(SUCCESS);
                        }
                    } catch (Exception e) {
                        checkLog.setResult(FAILED + e.getMessage());
                    }
                    logs.add(checkLog);
                });
        List<DevopsServiceE> devopsServiceES = devopsServiceRepository.list();
        devopsServiceES.parallelStream()
                .filter(t -> !ServiceStatus.DELETED.getStatus().equals(t.getStatus()))
                .forEach(devopsServiceE -> {
                    CheckLog checkLog = new CheckLog();
                    try {
                        checkLog.setContent("service: " + devopsServiceE.getName() + serialString);
                        V1Service service = getService(devopsServiceE);
                        DevopsEnvironmentE devopsEnvironmentE =
                                devopsEnvironmentRepository.queryById(devopsServiceE.getEnvId());
                        ObjectOperation<V1Service> objectOperation = new ObjectOperation<>();
                        objectOperation.setType(service);
                        Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
                        String filePath = "svc-" + devopsServiceE.getName();
                        if (!gitlabRepository.getFile(projectId, master, filePath + yamlFile)) {
                            objectOperation.operationEnvGitlabFile(
                                    filePath,
                                    projectId,
                                    CREATE,
                                    TypeUtil.objToLong(ADMIN), null, null, null, null);
                            checkLog.setResult(SUCCESS);
                        }
                    } catch (Exception e) {
                        checkLog.setResult(FAILED + e.getMessage());
                    }
                    logs.add(checkLog);
                });
        List<DevopsIngressE> devopsIngressES = devopsIngressRepository.list();
        devopsIngressES.parallelStream().forEach(devopsIngressE -> {
            CheckLog checkLog = new CheckLog();
            try {
                checkLog.setContent("ingress: " + devopsIngressE.getName() + serialString);
                DevopsEnvironmentE devopsEnvironmentE =
                        devopsEnvironmentRepository.queryById(devopsIngressE.getEnvId());
                ObjectOperation<V1beta1Ingress> objectOperation = new ObjectOperation<>();
                objectOperation.setType(getV1beta1Ingress(devopsIngressE));
                Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
                String filePath = "ing-" + devopsIngressE.getName();
                if (!gitlabRepository.getFile(projectId, master, filePath + yamlFile)) {
                    objectOperation.operationEnvGitlabFile(
                            filePath,
                            projectId,
                            CREATE,
                            TypeUtil.objToLong(ADMIN), null, null, null, null);
                    checkLog.setResult(SUCCESS);
                }
            } catch (Exception e) {
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
        c7nHelmRelease.getSpec().setValues(applicationInstanceService.getReplaceResult(applicationVersionRepository.queryValue(applicationVersionE.getId()),applicationInstanceRepository.queryValueByEnvIdAndAppId(
                applicationInstanceE.getDevopsEnvironmentE().getId(), applicationE.getId())).getDeltaYaml().trim());
        return c7nHelmRelease;
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
        final Integer[] serialNumber = {0};
        List<V1ServicePort> ports = devopsServiceE.getPorts().parallelStream()
                .map(t -> {
                    V1ServicePort v1ServicePort = new V1ServicePort();
                    if (t.getNodePort() != null) {
                        v1ServicePort.setNodePort(t.getNodePort().intValue());
                    }
                    if (t.getPort() != null) {
                        v1ServicePort.setPort(t.getPort().intValue());
                    }
                    if (t.getTargetPort() != null) {
                        v1ServicePort.setTargetPort(new IntOrString(t.getTargetPort().intValue()));
                    }
                    v1ServicePort.setName(t.getName() != null ? t.getName() : "http" + serialNumber[0]++);
                    v1ServicePort.setProtocol(t.getProtocol() != null ? t.getProtocol() : "TCP");
                    return v1ServicePort;
                }).collect(Collectors.toList());
        spec.setPorts(ports);

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

    private V1beta1Ingress getV1beta1Ingress(DevopsIngressE devopsIngressE) {
        V1beta1Ingress v1beta1Ingress = devopsIngressService
                .createIngress(devopsIngressE.getDomain(), devopsIngressE.getName());
        List<DevopsIngressPathE> devopsIngressPathES =
                devopsIngressRepository.selectByIngressId(devopsIngressE.getId());
        devopsIngressPathES.parallelStream()
                .forEach(devopsIngressPathE ->
                        v1beta1Ingress.getSpec().getRules().get(0).getHttp()
                                .addPathsItem(devopsIngressService
                                        .createPath(devopsIngressPathE.getPath(), devopsIngressPathE.getServiceId())));

        return v1beta1Ingress;
    }

    @Saga(code = "devops-upgrade-0.9",
            description = "devops smooth upgrade to 0.9", inputSchema = "{}")
    private void gitOpsUserAccess() {
        sagaClient.startSaga("devops-upgrade-0.9", new StartInstanceDTO("{}", "", ""));
    }
}
