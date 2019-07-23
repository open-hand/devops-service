package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.OperationPodPayload;
import io.choerodon.devops.app.eventhandler.payload.SecretPayLoad;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.domain.application.valueobject.ImagePullSecret;
import io.choerodon.devops.domain.application.valueobject.Payload;
import io.choerodon.devops.infra.dto.ApplicationDTO;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.HelmType;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;
import io.codearte.props2yaml.Props2YAML;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Created by younger on 2018/4/18.
 */
@Service
public class AgentCommandServiceImpl implements AgentCommandService {
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
    @Lazy
    private CommandSender commandSender;

    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private GitUtil gitUtil;

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


    @Override
    public void sendCommand(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        Msg msg = new Msg();
        msg.setKey("cluster:" + devopsEnvironmentDTO.getClusterId() + ".env:" + devopsEnvironmentDTO.getCode() + ".envId:" + devopsEnvironmentDTO.getId());
        msg.setType("git_ops_sync");
        msg.setPayload("");
        commandSender.sendMsg(msg);
    }


    @Override
    public void deploy(ApplicationDTO applicationDTO, ApplicationVersionDTO applicationVersionDTO, String releaseName, DevopsEnvironmentDTO devopsEnvironmentDTO, String values, Long commandId, String secretCode) {
        Msg msg = new Msg();
        List<ImagePullSecret> imagePullSecrets = null;
        if (secretCode != null) {
            imagePullSecrets = Arrays.asList(new ImagePullSecret(secretCode));
        }
        Payload payload = new Payload(
                devopsEnvironmentDTO.getCode(),
                applicationVersionDTO.getRepository(),
                applicationDTO.getCode(),
                applicationVersionDTO.getVersion(),
                values, releaseName, imagePullSecrets);

        msg.setKey(String.format("cluster:%d.env:%s.envId:%d.release:%s",
                devopsEnvironmentDTO.getClusterId(),
                devopsEnvironmentDTO.getCode(),
                devopsEnvironmentDTO.getId(),
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
    public void upgradeCluster(DevopsClusterDTO devopsClusterDTO) {
        Msg msg = new Msg();
        Map<String, String> configs = new HashMap<>();
        configs.put("config.connect", agentServiceUrl);
        configs.put("config.token", devopsClusterDTO.getToken());
        configs.put("config.clusterId", devopsClusterDTO.getId().toString());
        configs.put("config.choerodonId", devopsClusterDTO.getChoerodonId());
        configs.put("rbac.create", "true");
        Payload payload = new Payload(
                "choerodon",
                agentRepoUrl,
                "choerodon-cluster-agent",
                agentExpectVersion,
                Props2YAML.fromContent(FileUtil.propertiesToString(configs))
                        .convert(), "choerodon-cluster-agent-" + devopsClusterDTO.getCode(), null);
        msg.setKey(String.format(KEY_FORMAT,
                devopsClusterDTO.getId(),
                "choerodon-cluster-agent-" + devopsClusterDTO.getCode()));
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
                null, "choerodon-cert-manager", null);
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
    public void operateSecret(Long clusterId, String namespace, String secretName, ProjectConfigVO projectConfigVO, String Type) {
        Msg msg = new Msg();
        SecretPayLoad secretPayLoad = new SecretPayLoad();
        secretPayLoad.setEmail(projectConfigVO.getEmail());
        secretPayLoad.setName(secretName);
        secretPayLoad.setNamespace(namespace);
        secretPayLoad.setServer(projectConfigVO.getUrl());
        secretPayLoad.setUsername(projectConfigVO.getUserName());
        secretPayLoad.setPassword(projectConfigVO.getPassword());

        try {
            msg.setPayload(mapper.writeValueAsString(secretPayLoad));
        } catch (IOException e) {
            throw new CommonException(ERROR_PAYLOAD_ERROR, e);
        }

        msg.setType(OPERATE_DOCKER_REGISTRY_SECRET);
        msg.setKey(String.format("cluster:%s.env:%s.Secret:%s", clusterId, namespace, secretName
        ));
        commandSender.sendMsg(msg);
    }

    @Override
    public void initCluster(Long clusterId) {
        GitConfigVO gitConfigVO = gitUtil.getGitConfig(clusterId);
        Msg msg = new Msg();
        try {
            msg.setPayload(mapper.writeValueAsString(gitConfigVO));
        } catch (IOException e) {
            throw new CommonException("read envId from agent session failed", e);
        }
        msg.setType(INIT_AGENT);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId
        ));
        commandSender.sendMsg(msg);

    }

    @Override
    public void initEnv(DevopsEnvironmentDTO devopsEnvironmentDTO, Long clusterId) {
        GitConfigVO gitConfigVO = gitUtil.getGitConfig(clusterId);
        List<GitEnvConfigVO> gitEnvConfigVOS = new ArrayList<>();
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(devopsEnvironmentDTO.getProjectId());
        OrganizationDTO organization = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String repoUrl = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organization.getCode(), projectDTO.getCode(), devopsEnvironmentDTO.getCode());

        GitEnvConfigVO gitEnvConfigVO = new GitEnvConfigVO();
        gitEnvConfigVO.setEnvId(devopsEnvironmentDTO.getId());
        gitEnvConfigVO.setGitRsaKey(devopsEnvironmentDTO.getEnvIdRsa());
        gitEnvConfigVO.setGitUrl(repoUrl);
        gitEnvConfigVO.setNamespace(devopsEnvironmentDTO.getCode());
        gitEnvConfigVOS.add(gitEnvConfigVO);
        gitConfigVO.setEnvs(gitEnvConfigVOS);
        gitConfigVO.setGitHost(gitlabSshUrl);
        Msg msg = new Msg();
        try {
            msg.setPayload(mapper.writeValueAsString(gitConfigVO));
        } catch (IOException e) {
            throw new CommonException("read envId from agent session failed", e);
        }
        msg.setType(INIT_ENV);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId
        ));
        commandSender.sendMsg(msg);
    }

    @Override
    public void deployTestApp(ApplicationDTO applicationDTO, ApplicationVersionDTO applicationVersionDTO, String releaseName, String secretName, Long clusterId, String values) {
        Msg msg = new Msg();
        List<ImagePullSecret> imagePullSecrets = Arrays.asList(new ImagePullSecret(secretName));
        Payload payload = new Payload(
                null,
                applicationVersionDTO.getRepository(),
                applicationDTO.getCode(),
                applicationVersionDTO.getVersion(),
                values, releaseName, imagePullSecrets);
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
        List<Long> connected = clusterConnectionHandler.getConnectedEnvList();
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
        GitEnvConfigVO gitEnvConfigVO = new GitEnvConfigVO();
        gitEnvConfigVO.setEnvId(envId);
        gitEnvConfigVO.setNamespace(code);
        Msg msg = new Msg();
        try {
            msg.setPayload(mapper.writeValueAsString(gitEnvConfigVO));
        } catch (IOException e) {
            throw new CommonException("error get envId and code", e);
        }
        msg.setType(DELETE_ENV);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId));
        commandSender.sendMsg(msg);
    }


}
