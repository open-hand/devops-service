package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServicePort;
import io.kubernetes.client.models.V1ServiceSpec;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
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

    @Override
    @Async
    public void checkLog(String version) {
        switch (version) {
            case "0.8":
                List<ApplicationDO> applications = applicationMapper.selectAll();
                DevopsCheckLogE devopsCheckLogE = new DevopsCheckLogE();
                devopsCheckLogE.setBeginCheckDate(new Date());
                List<CheckLog> logs = new ArrayList<>();
                applications.parallelStream()
                        .filter(applicationDO ->
                                applicationDO.getGitlabProjectId() != null && applicationDO.getHookId() == null)
                        .forEach(applicationDO -> {
                            syncWebHook(applicationDO, logs);
                            syncBranches(applicationDO, logs);
                        });
                devopsCheckLogE.setLog(JSON.toJSONString(logs));
                devopsCheckLogE.setEndCheckDate(new Date());
                devopsCheckLogRepository.create(devopsCheckLogE);
                break;
            case "0.9":
                syncNonEnvGroupProject();
                syncEnvProject();
                break;
            default:
                break;
        }
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
            checkLog.setResult("success");
        } catch (Exception e) {
            checkLog.setResult("failed: " + e.getMessage());
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
                        checkLog.setResult("success");
                    }));
        } catch (Exception e) {
            checkLog.setResult("failed: " + e.getMessage());
        }
        logs.add(checkLog);
    }


    private void syncNonEnvGroupProject() {
        List<DevopsProjectDO> projectDOList = devopsCheckLogRepository.queryNonEnvGroupProject();
        final String groupCodeSuffix = "gitops";
        projectDOList.parallelStream()
                .forEach(t -> {
                    Long projectId = t.getId();
                    ProjectE projectE = iamRepository.queryIamProject(projectId);
                    Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
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
                });
    }


    private void syncEnvProject() {
        List<DevopsEnvironmentE> devopsEnvironmentES = devopsEnvironmentRepository.list();
        devopsEnvironmentES.parallelStream().filter(devopsEnvironmentE -> devopsEnvironmentE.getGitlabEnvProjectId() == null).forEach(devopsEnvironmentE -> {
            ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            List<String> sshKeys = FileUtil.getSshKey(
                    organization.getCode() + "/" + projectE.getCode() + "/" + devopsEnvironmentE.getCode());
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
        });
    }


    private void syncObjects() {
        List<ApplicationInstanceE> applicationInstanceES = applicationInstanceRepository.list();
        applicationInstanceES.parallelStream().filter(applicationInstanceE -> !applicationInstanceE.getStatus().equals(InstanceStatus.DELETED.getStatus())).forEach(applicationInstanceE -> {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(applicationInstanceE.getDevopsEnvironmentE().getId());
            ObjectOperation<C7nHelmRelease> objectOperation = new ObjectOperation<>();
            objectOperation.setType(getC7NHelmRelease(applicationInstanceE));
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    "release-" + applicationInstanceE.getCode(),
                    projectId,
                    CREATE,
                    TypeUtil.objToLong(ADMIN));
        });
        List<DevopsServiceE> devopsServiceES = devopsServiceRepository.list();
        devopsServiceES.parallelStream().filter(devopsServiceE -> !devopsServiceE.getStatus().equals(ServiceStatus.DELETED)).forEach(devopsServiceE -> {
            getService(devopsServiceE);
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(devopsServiceE.getEnvId());
            ObjectOperation<V1Service> objectOperation = new ObjectOperation<>();
            objectOperation.setType(getService(devopsServiceE));
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            objectOperation.operationEnvGitlabFile(
                    "svc-" + devopsServiceE.getName(),
                    projectId,
                    CREATE,
                    TypeUtil.objToLong(ADMIN));
        });
    }

    private C7nHelmRelease getC7NHelmRelease(ApplicationInstanceE applicationInstanceE) {
        ApplicationVersionE applicationVersionE = applicationVersionRepository.query(applicationInstanceE.getApplicationVersionE().getId());
        ApplicationE applicationE = applicationRepository.query(applicationInstanceE.getApplicationE().getId());
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(applicationInstanceE.getCode());
        c7nHelmRelease.getSpec().setRepoUrl(helmUrl + applicationVersionE.getRepository());
        c7nHelmRelease.getSpec().setChartName(applicationE.getCode());
        c7nHelmRelease.getSpec().setChartVersion(applicationVersionE.getVersion());
        c7nHelmRelease.getSpec().setValues(applicationInstanceRepository.queryValueByEnvIdAndAppId(applicationInstanceE.getDevopsEnvironmentE().getId(), applicationE.getId()));
        return c7nHelmRelease;
    }

    private V1Service getService(DevopsServiceE devopsServiceE) {
        Map<String, String> annotations = gson.fromJson(devopsServiceE.getAnnotations(), new TypeToken<Map<String, String>>() {
        }.getType());
        V1Service service = new V1Service();
        service.setKind("Service");
        service.setApiVersion("v1");
        Map<String, String> label = new HashMap<>();
        label.put(SERVICE_LABLE, SERVICE);
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setLabels(label);
        metadata.setName(devopsServiceE.getName());
        metadata.setAnnotations(annotations);
        service.setMetadata(metadata);

        V1ServiceSpec spec = new V1ServiceSpec();
        List<V1ServicePort> ports = new ArrayList<>();
        V1ServicePort v1ServicePort = new V1ServicePort();
        v1ServicePort.setName("http");
        v1ServicePort.setPort(devopsServiceE.getPort().intValue());
        v1ServicePort.setTargetPort(new IntOrString(devopsServiceE.getPort().intValue()));
        v1ServicePort.setProtocol("TCP");
        ports.add(v1ServicePort);

        if (!StringUtils.isEmpty(devopsServiceE.getExternalIp())) {
            List<String> externallIps = new ArrayList<>();
            externallIps.add(devopsServiceE.getExternalIp());
            spec.setExternalIPs(externallIps);
        }

        spec.setPorts(ports);
        spec.setSessionAffinity("None");
        spec.type("ClusterIP");
        service.setSpec(spec);
        return service;
    }
}
