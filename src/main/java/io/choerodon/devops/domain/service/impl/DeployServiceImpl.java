package io.choerodon.devops.domain.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.Payload;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.common.util.enums.HelmType;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;
import io.choerodon.websocket.helper.EnvListener;
import io.codearte.props2yaml.Props2YAML;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by younger on 2018/4/18.
 */
@Service
public class DeployServiceImpl implements DeployService {
    private static final String ERROR_PAYLOAD_ERROR = "error.payload.error";
    private static final String KEY_FORMAT = "cluster:%d.release:%s";
    private static final String CLUSTER_FORMAT = "cluster:%s";

    private static final String INIT_AGENT = "init_agent";
    private static final String DELETE_ENV = "delete_env";
    private static final String INIT_ENV = "create_env";
    private static final String OPERATE_POD_COUNT = "operate_pod_count";
    private static final String OPERATE_DOCKER_REGISTRY_SECRET = "operate_docker_registry_secret";
    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private CommandSender commandSender;

    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private EnvListener envListener;

    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${agent.repoUrl}")
    private String agentRepoUrl;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;
    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${agent.serviceUrl}")
    private String agentServiceUrl;

    @Autowired
    public DeployServiceImpl(CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    @Override
    public void sendCommand(DevopsEnvironmentE devopsEnvironmentE) {
        Msg msg = new Msg();
        msg.setKey("cluster:" + devopsEnvironmentE.getClusterE().getId() + ".env:" + devopsEnvironmentE.getCode() + ".envId:" + devopsEnvironmentE.getId());
        msg.setType("git_ops_sync");
        msg.setPayload("");
        commandSender.sendMsg(msg);
    }


    @Override
    public void deploy(ApplicationE applicationE, ApplicationVersionE applicationVersionE, String releaseName, DevopsEnvironmentE devopsEnvironmentE, String values, Long commandId) {
        Msg msg = new Msg();
        Payload payload = new Payload(
                devopsEnvironmentE.getCode(),
                applicationVersionE.getRepository(),
                applicationE.getCode(),
                applicationVersionE.getVersion(),
                values, releaseName);
        msg.setKey(String.format("cluster:%d.env:%s.envId:%d.release:%s",
                devopsEnvironmentE.getClusterE().getId(),
                devopsEnvironmentE.getCode(),
                devopsEnvironmentE.getId(),
                releaseName));
        msg.setType(HelmType.HELM_RELEASE_PRE_UPGRADE.toValue());
        try {
            msg.setPayload(mapper.writeValueAsString(payload));
            msg.setCommandId(commandId);
        } catch (IOException e) {
            throw new CommonException(ERROR_PAYLOAD_ERROR, e);
        }
        commandSender.sendMsg(msg);
    }

    @Override
    public void upgradeCluster(DevopsClusterE devopsClusterE) {
        Msg msg = new Msg();
        Map<String, String> configs = new HashMap<>();
        configs.put("config.connect", agentServiceUrl);
        configs.put("config.token", devopsClusterE.getToken());
        configs.put("config.clusterId", devopsClusterE.getId().toString());
        configs.put("config.choerodonId", devopsClusterE.getChoerodonId());
        configs.put("rbac.create", "true");
        Payload payload = new Payload(
                "choerodon",
                agentRepoUrl,
                "choerodon-cluster-agent",
                agentExpectVersion,
                Props2YAML.fromContent(FileUtil.propertiesToString(configs))
                        .convert(), "choerodon-cluster-agent-" + devopsClusterE.getCode());
        msg.setKey(String.format(KEY_FORMAT,
                devopsClusterE.getId(),
                "choerodon-cluster-agent-" + devopsClusterE.getCode()));
        msg.setType(HelmType.HELM_RELEASE_UPGRADE.toValue());
        try {
            msg.setPayload(mapper.writeValueAsString(payload));
        } catch (IOException e) {
            throw new CommonException(ERROR_PAYLOAD_ERROR, e);
        }
        commandSender.sendMsg(msg);
    }

    @Override
    public void createCertManager(Long clusterId) {
        Msg msg = new Msg();
        Payload payload = new Payload(
                "kube-system",
                agentRepoUrl,
                "cert-manager",
                agentExpectVersion,
                null, "choerodon-cert-manager");
        msg.setKey(String.format(KEY_FORMAT, clusterId, "choerodon-cert-manager"));
        msg.setType(HelmType.HELM_INSTALL_RELEASE.toValue());
        try {
            msg.setPayload(mapper.writeValueAsString(payload));
        } catch (IOException e) {
            throw new CommonException(ERROR_PAYLOAD_ERROR, e);
        }
        commandSender.sendMsg(msg);
    }

    @Override
    public void operatePodCount(String deploymentName, String namespace, Long clusterId, Long count) {
        Msg msg = new Msg();
        OperationPodPayload operationPodPayload = new OperationPodPayload();
        operationPodPayload.setCount(count);
        operationPodPayload.setDeploymentName(deploymentName);
        operationPodPayload.setNamespace(namespace);
        try {
            msg.setPayload(mapper.writeValueAsString(operationPodPayload));
        } catch (IOException e) {
            throw new CommonException(ERROR_PAYLOAD_ERROR, e);
        }
        msg.setType(OPERATE_POD_COUNT);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId
        ));
        commandSender.sendMsg(msg);
    }

    @Override
    public void operateSecret(Long clusterId, String namespace, ProjectConfigDTO projectConfigDTO, String Type) {
        Msg msg = new Msg();
        SecretPayLoad secretPayLoad = new SecretPayLoad();
        secretPayLoad.setEmail(projectConfigDTO.getEmail());
        secretPayLoad.setName(projectConfigDTO.getProject());
        secretPayLoad.setNamespace(namespace);
        secretPayLoad.setServer(projectConfigDTO.getUrl());
        secretPayLoad.setUsername(projectConfigDTO.getUserName());
        secretPayLoad.setPassword(projectConfigDTO.getPassword());

        try {
            msg.setPayload(mapper.writeValueAsString(secretPayLoad));
        } catch (IOException e) {
            throw new CommonException(ERROR_PAYLOAD_ERROR, e);
        }

        msg.setType(OPERATE_DOCKER_REGISTRY_SECRET);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId
        ));
        commandSender.sendMsg(msg);
    }

    @Override
    public void initCluster(Long clusterId) {
        GitConfigDTO gitConfigDTO = envUtil.getGitConfig(clusterId);
        Msg msg = new Msg();
        try {
            msg.setPayload(mapper.writeValueAsString(gitConfigDTO));
        } catch (IOException e) {
            throw new CommonException("read envId from agent session failed", e);
        }
        msg.setType(INIT_AGENT);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId
        ));
        commandSender.sendMsg(msg);

    }

    @Override
    public void initEnv(DevopsEnvironmentE devopsEnvironmentE, Long clusterId) {
        GitConfigDTO gitConfigDTO = envUtil.getGitConfig(clusterId);
        List<GitEnvConfigDTO> gitEnvConfigDTOS = new ArrayList<>();
        ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String repoUrl = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());

        GitEnvConfigDTO gitEnvConfigDTO = new GitEnvConfigDTO();
        gitEnvConfigDTO.setEnvId(devopsEnvironmentE.getId());
        gitEnvConfigDTO.setGitRsaKey(devopsEnvironmentE.getEnvIdRsa());
        gitEnvConfigDTO.setGitUrl(repoUrl);
        gitEnvConfigDTO.setNamespace(devopsEnvironmentE.getCode());
        gitEnvConfigDTOS.add(gitEnvConfigDTO);
        gitConfigDTO.setEnvs(gitEnvConfigDTOS);
        gitConfigDTO.setGitHost(gitlabSshUrl);
        Msg msg = new Msg();
        try {
            msg.setPayload(mapper.writeValueAsString(gitConfigDTO));
        } catch (IOException e) {
            throw new CommonException("read envId from agent session failed", e);
        }
        msg.setType(INIT_ENV);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId
        ));
        commandSender.sendMsg(msg);
    }

    @Override
    public void deployTestApp(ApplicationE applicationE, ApplicationVersionE applicationVersionE, String releaseName, Long clusterId, String values) {
        Msg msg = new Msg();
        Payload payload = new Payload(
                null,
                applicationVersionE.getRepository(),
                applicationE.getCode(),
                applicationVersionE.getVersion(),
                values, releaseName);
        msg.setKey(String.format(KEY_FORMAT, clusterId, releaseName));
        msg.setType(HelmType.EXECUTE_TEST.toValue());
        try {
            msg.setPayload(mapper.writeValueAsString(payload));
        } catch (IOException e) {
            throw new CommonException(ERROR_PAYLOAD_ERROR, e);
        }
        commandSender.sendMsg(msg);

    }

    @Override
    public void getTestAppStatus(Map<Long, List<String>> testReleases) {
        List<Long> connected = envUtil.getConnectedEnvList(envListener);
        testReleases.forEach((key, value) -> {
            if (connected.contains(key)) {
                Msg msg = new Msg();
                msg.setKey(String.format("cluster:%d",
                        key));
                msg.setPayload(JSONArray.toJSONString(value));
                msg.setType(HelmType.TEST_STATUS.toValue());
                commandSender.sendMsg(msg);
            }
        });
    }

    @Override
    public void deleteEnv(Long envId, String code, Long clusterId) {
        GitEnvConfigDTO gitEnvConfigDTO = new GitEnvConfigDTO();
        gitEnvConfigDTO.setEnvId(envId);
        gitEnvConfigDTO.setNamespace(code);
        Msg msg = new Msg();
        try {
            msg.setPayload(mapper.writeValueAsString(gitEnvConfigDTO));
        } catch (IOException e) {
            throw new CommonException("error get envId and code", e);
        }
        msg.setType(DELETE_ENV);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId));
        commandSender.sendMsg(msg);
    }


}
