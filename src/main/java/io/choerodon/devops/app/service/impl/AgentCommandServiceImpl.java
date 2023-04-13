package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import io.codearte.props2yaml.Props2YAML;
import org.hzero.websocket.constant.ClientWebSocketConstant;
import org.hzero.websocket.helper.KeySocketSendHelper;
import org.hzero.websocket.vo.MsgVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.Command;
import io.choerodon.devops.api.vo.kubernetes.ImagePullSecret;
import io.choerodon.devops.api.vo.kubernetes.Payload;
import io.choerodon.devops.app.eventhandler.constants.CertManagerConstants;
import io.choerodon.devops.app.eventhandler.payload.DeleteHelmHookJobRequest;
import io.choerodon.devops.app.eventhandler.payload.OperationPodPayload;
import io.choerodon.devops.app.eventhandler.payload.SecretPayLoad;
import io.choerodon.devops.app.service.AgentCommandService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.EnvironmentType;
import io.choerodon.devops.infra.enums.HelmType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;
import io.choerodon.devops.infra.util.*;


/**
 * Created by younger on 2018/4/18.
 */
@Service
public class AgentCommandServiceImpl implements AgentCommandService {

    public static final Logger LOGGER = LoggerFactory.getLogger(AgentCommandServiceImpl.class);

    private static final String KEY_FORMAT = "cluster:%d.release:%s";
    private static final String CLUSTER_FORMAT = "cluster:%s";
    private static final String CLUSTER = "cluster:";

    private static final String AGENT_INIT = "agent_init";
    private static final String ENV_DELETE = "env_delete";
    private static final String ENV_CREATE = "env_create";
    private static final String RESOURCE_DESCRIBE = "resource_describe";
    private static final String HELM_RELEASE_UPGRADE = "helm_release_upgrade";
    private static final String OPERATE_POD_COUNT = "operate_pod_count";

    private static final String DELETE_HELM_HOOK_JOB = "delete_helm_hook_job";
    private static final String OPERATE_DOCKER_REGISTRY_SECRET = "operate_docker_registry_secret";
    private static final String CLUSTER_AGENT = "choerodon-cluster-agent-";
    private static final String RESTART_AGENT = "restart_agent";


    private static final Pattern PATTERN = Pattern.compile("^[-+]?[\\d]*$");
    private static final Gson gson = new Gson();


    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    @Lazy
    private KeySocketSendHelper webSocketHelper;

    @Value("${agent.repoUrl}")
    private String agentRepoUrl;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;
    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${agent.serviceUrl}")
    private String agentServiceUrl;
    @Value("${agent.repoConcurrencySyncSize:1}")
    private Integer repoConcurrencySyncSize;


    @Override
    public void sendCommand(DevopsEnvironmentDTO devopsEnvironmentDTO) {
        AgentMsgVO msg = new AgentMsgVO();
        String namespace = GitOpsUtil.getEnvNamespace(Objects.requireNonNull(devopsEnvironmentDTO.getCode()), Objects.requireNonNull(devopsEnvironmentDTO.getType()));
        msg.setKey(CLUSTER + devopsEnvironmentDTO.getClusterId() + ".env:" + namespace + ".envId:" + devopsEnvironmentDTO.getId());
        msg.setType("git_ops_sync");
        msg.setPayload("");
        sendToWebSocket(devopsEnvironmentDTO.getClusterId(), msg);
    }


    @Override
    public void deploy(AppServiceDTO appServiceDTO, AppServiceVersionDTO appServiceVersionDTO, String releaseName, DevopsEnvironmentDTO devopsEnvironmentDTO, String values, Long commandId, String secretCode) {
        AgentMsgVO msg = new AgentMsgVO();
        List<ImagePullSecret> imagePullSecrets = null;
        if (secretCode != null) {
            imagePullSecrets = ArrayUtil.singleAsList(new ImagePullSecret(secretCode));
        }
        Payload payload = new Payload(
                devopsEnvironmentDTO.getCode(),
                appServiceVersionDTO.getRepository(),
                appServiceDTO.getCode(),
                appServiceVersionDTO.getVersion(),
                values, releaseName, imagePullSecrets);

        msg.setKey(String.format("cluster:%d.env:%s.envId:%d.release:%s",
                devopsEnvironmentDTO.getClusterId(),
                devopsEnvironmentDTO.getCode(),
                devopsEnvironmentDTO.getId(),
                releaseName));

        msg.setType(HelmType.HELM_UPGRADE_JOB_INFO.toValue());
        msg.setPayload(JsonHelper.marshalByJackson(payload));
        msg.setCommandId(commandId);
        sendToWebSocket(devopsEnvironmentDTO.getClusterId(), msg);
    }

    @Override
    public void upgradeCluster(DevopsClusterDTO devopsClusterDTO, WebSocketSession webSocketSession) {
        AgentMsgVO msg = new AgentMsgVO();
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
                        .convert(), CLUSTER_AGENT + devopsClusterDTO.getCode(), null);
        msg.setKey(String.format(KEY_FORMAT,
                devopsClusterDTO.getId(),
                CLUSTER_AGENT + devopsClusterDTO.getCode()));
        msg.setType(HELM_RELEASE_UPGRADE);
        msg.setPayload(JsonHelper.marshalByJackson(payload));
        String msgPayload = JsonHelper.marshalByJackson(msg);

        // 暂时不使用新的WebSocket消息格式重写升级消息
        // 一开始没有自动升级
        //0.18.0到0.19.0 为了agent的平滑升级，所以不能以通用的新Msg方式发送，继续用以前的Msg格式发送
        sendToSession(webSocketSession, new TextMessage(msgPayload));
    }

    @Override
    public void newUpgradeCluster(DevopsClusterDTO devopsClusterDTO, WebSocketSession webSocketSession) {
        AgentMsgVO msg = buildAgentUpgradeMessage(devopsClusterDTO);
        // 再包装一层, 统一所有的消息结构
        MsgVO finalMessage = (new MsgVO()).setGroup(CLUSTER + devopsClusterDTO.getId()).setKey(msg.getKey()).setMessage(JsonHelper.marshalByJackson(msg));
        sendToSession(webSocketSession, new TextMessage(JsonHelper.marshalByJackson(finalMessage)));
    }

    private AgentMsgVO buildAgentUpgradeMessage(DevopsClusterDTO devopsClusterDTO) {
        // 查询用户相关的邮箱（这里可以考虑多个集群同时连接造成的并发请求iam，可以考虑别的机制减少请求次数）
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(devopsClusterDTO.getCreatedBy());
        AgentMsgVO msg = new AgentMsgVO();
        Map<String, Object> configs = new HashMap<>();
        Map<String, String> configValues = new HashMap<>();
        configValues.put("connect", agentServiceUrl);
        configValues.put("token", devopsClusterDTO.getToken());
        configValues.put("clusterId", devopsClusterDTO.getId().toString());
        configValues.put("choerodonId", devopsClusterDTO.getChoerodonId());
        configValues.put("email", iamUserDTO.getEmail());
        Map<String, String> rbacValues = new HashMap<>();
        rbacValues.put("create", "true");
        configs.put("config", configValues);
        configs.put("rbac", rbacValues);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);


        Yaml yaml = new Yaml(options);

        Payload payload = new Payload(
                "choerodon",
                agentRepoUrl,
                "choerodon-cluster-agent",
                agentExpectVersion,
                yaml.dump(configs), CLUSTER_AGENT + devopsClusterDTO.getCode(), null);
        msg.setKey(String.format(KEY_FORMAT,
                devopsClusterDTO.getId(),
                CLUSTER_AGENT + devopsClusterDTO.getCode()));
        msg.setType(HELM_RELEASE_UPGRADE);
        msg.setPayload(JsonHelper.marshalByJackson(payload));
        return msg;
    }

    @Override
    public void installCertManager(String certManagerRepoUrl, Long clusterId, String certManagerReleaseName, String namespace, String chartVersion) {
        AgentMsgVO msg = new AgentMsgVO();
        Payload payload = new Payload(
                namespace,
                certManagerRepoUrl,
                "cert-manager",
                chartVersion,
                null, certManagerReleaseName, null);
        msg.setKey(String.format(KEY_FORMAT, clusterId, certManagerReleaseName));
        msg.setType(HelmType.CERT_MANAGER_INSTALL.toValue());
        msg.setPayload(JsonHelper.marshalByJackson(payload));
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void operatePodCount(String kind, String name, String namespace, Long clusterId, Long count, String commandId) {
        AgentMsgVO msg = new AgentMsgVO();
        OperationPodPayload operationPodPayload = new OperationPodPayload();
        operationPodPayload.setCount(count);
        operationPodPayload.setCommandId(commandId);
        operationPodPayload.setName(name);
        operationPodPayload.setKind(kind);
        operationPodPayload.setNamespace(namespace);
        msg.setPayload(JsonHelper.marshalByJackson(operationPodPayload));
        msg.setType(OPERATE_POD_COUNT);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId
        ));
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void deleteHelmHookJob(String name, String namespace, Long clusterId) {
        AgentMsgVO msg = new AgentMsgVO();
        DeleteHelmHookJobRequest operationPodPayload = new DeleteHelmHookJobRequest();
        operationPodPayload.setJobName(name);
        operationPodPayload.setNamespace(namespace);
        msg.setPayload(JsonHelper.marshalByJackson(operationPodPayload));
        msg.setType(DELETE_HELM_HOOK_JOB);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId));
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void operateSecret(Long clusterId, String namespace, String secretName, ConfigVO configVO) {
        AgentMsgVO msg = new AgentMsgVO();
        SecretPayLoad secretPayLoad = new SecretPayLoad();
        secretPayLoad.setEmail(configVO.getEmail());
        secretPayLoad.setName(secretName);
        secretPayLoad.setNamespace(namespace);
        secretPayLoad.setServer(configVO.getUrl());
        secretPayLoad.setUsername(configVO.getUserName());
        secretPayLoad.setPassword(configVO.getPassword());

        msg.setPayload(JsonHelper.marshalByJackson(secretPayLoad));

        msg.setType(OPERATE_DOCKER_REGISTRY_SECRET);
        msg.setKey(String.format("cluster:%s.env:%s.Secret:%s", clusterId, namespace, secretName
        ));
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void gitopsSyncCommandStatus(Long clusterId, String envCode, Long envId, List<Command> commands) {

        AgentMsgVO msg = new AgentMsgVO();
        msg.setKey(String.format("cluster:%d.env:%s.envId:%d",
                clusterId,
                envCode,
                envId));
        msg.setType(HelmType.RESOURCE_STATUS_SYNC.toValue());
        msg.setPayload(JsonHelper.marshalByJackson(commands));
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void startOrStopInstance(String payload, String name, String type, String namespace, Long commandId, Long envId, Long clusterId) {
        AgentMsgVO msg = new AgentMsgVO();
        String key = CLUSTER + clusterId + ".env:" + namespace + ".envId:" + envId + ".release:" + name;
        msg.setKey(key);
        msg.setType(type);
        msg.setPayload(payload);
        msg.setCommandId(commandId);
        LOGGER.debug("Sending {} command. The key is: {}. THe commandId is: {}. The payload is {}. ", type, key, commandId, payload);
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void startLogOrExecConnection(String type, String key, PipeRequestVO pipeRequest, Long clusterId) {
        AgentMsgVO agentMsgVO = new AgentMsgVO(key, type, JsonHelper.marshalByJackson(pipeRequest));
        sendToWebSocket(clusterId, agentMsgVO);
    }


    @Override
    public void startDescribeConnection(String key, DescribeResourceVO describeResourceVO, Long clusterId) {
        AgentMsgVO agentMsgVO = new AgentMsgVO(key, RESOURCE_DESCRIBE, JsonHelper.marshalByJackson(describeResourceVO));
        sendToWebSocket(clusterId, agentMsgVO);
    }

    @Override
    public void initCluster(Long clusterId, WebSocketSession webSocketSession) {
        GitConfigVO gitConfigVO = gitUtil.getGitConfig(clusterId);
        gitConfigVO.setRepoConcurrencySyncSize(repoConcurrencySyncSize);
        AgentMsgVO msg = new AgentMsgVO();
        msg.setPayload(JsonHelper.marshalByJackson(gitConfigVO));
        msg.setType(AGENT_INIT);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId
        ));
        // 为了保持和其他通过hzero发送的消息结构一致
        MsgVO msgVO = (new MsgVO()).setGroup(CLUSTER + clusterId).setKey(AGENT_INIT).setMessage(JsonHelper.marshalByJackson(msg)).setType(ClientWebSocketConstant.SendType.S_GROUP);

        sendToSession(webSocketSession, new TextMessage(JsonHelper.marshalByJackson(msgVO)));
    }

    private void sendToSession(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
        if (webSocketSession.isOpen()) {
            synchronized (webSocketSession) {
                try {
                    webSocketSession.sendMessage(webSocketMessage);
                } catch (IOException e) {
                    LOGGER.warn("Send to session: Failed to send message. the message is {}, and the ex is: ", webSocketMessage.getPayload(), e);
                }
            }
        } else {
            LOGGER.warn("Send to session: session is unexpectedly closed. the message is {}", webSocketMessage.getPayload());
        }
    }

    @Override
    public void initEnv(DevopsEnvironmentDTO devopsEnvironmentDTO, Long clusterId) {
        GitConfigVO gitConfigVO = gitUtil.getGitConfig(clusterId);
        List<GitEnvConfigVO> gitEnvConfigVOS = new ArrayList<>();
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String repoUrl = GitUtil.getGitlabSshUrl(PATTERN, gitlabSshUrl, organization.getTenantNum(),
                projectDTO.getDevopsComponentCode(), devopsEnvironmentDTO.getCode(),
                EnvironmentType.forValue(devopsEnvironmentDTO.getType()),
                devopsClusterMapper.selectByPrimaryKey(devopsEnvironmentDTO.getClusterId()).getCode());

        GitEnvConfigVO gitEnvConfigVO = new GitEnvConfigVO();
        gitEnvConfigVO.setEnvId(devopsEnvironmentDTO.getId());
        gitEnvConfigVO.setGitRsaKey(devopsEnvironmentDTO.getEnvIdRsa());
        gitEnvConfigVO.setGitUrl(repoUrl);
        gitEnvConfigVO.setNamespace(GitOpsUtil.getEnvNamespace(devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getType()));
        gitEnvConfigVOS.add(gitEnvConfigVO);
        gitConfigVO.setEnvs(gitEnvConfigVOS);
        gitConfigVO.setGitHost(gitlabSshUrl);
        AgentMsgVO msg = new AgentMsgVO();
        msg.setPayload(JsonHelper.marshalByJackson(gitConfigVO));
        msg.setType(ENV_CREATE);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId
        ));
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void deployTestApp(AppServiceDTO appServiceDTO, AppServiceVersionDTO appServiceVersionDTO, String releaseName, String secretName, Long clusterId, String values) {
        AgentMsgVO msg = new AgentMsgVO();
        List<ImagePullSecret> imagePullSecrets = ArrayUtil.singleAsList(new ImagePullSecret(secretName));
        Payload payload = new Payload(
                null,
                appServiceVersionDTO.getRepository(),
                appServiceDTO.getCode(),
                appServiceVersionDTO.getVersion(),
                values, releaseName, imagePullSecrets);
        msg.setKey(String.format(KEY_FORMAT, clusterId, releaseName));
        msg.setType(HelmType.TEST_EXECUTE.toValue());
        msg.setPayload(JsonHelper.marshalByJackson(payload));
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void getTestAppStatus(Map<Long, List<String>> testReleases) {
        List<Long> connected = clusterConnectionHandler.getUpdatedClusterList();
        testReleases.forEach((key, value) -> {
            if (connected.contains(key)) {
                AgentMsgVO msg = new AgentMsgVO();
                msg.setKey(String.format("cluster:%d",
                        key));
                msg.setPayload(JsonHelper.marshalByJackson(value));
                msg.setType(HelmType.TEST_STATUS.toValue());
                sendToWebSocket(key, msg);
            }
        });
    }

    @Override
    public void deleteEnv(Long envId, String code, Long clusterId) {
        GitEnvConfigVO gitEnvConfigVO = new GitEnvConfigVO();
        gitEnvConfigVO.setEnvId(envId);
        gitEnvConfigVO.setNamespace(code);
        AgentMsgVO msg = new AgentMsgVO();
        msg.setPayload(JsonHelper.marshalByJackson(gitEnvConfigVO));
        msg.setType(ENV_DELETE);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId));
        sendToWebSocket(clusterId, msg);
    }

    private void sendToWebSocket(Long clusterId, AgentMsgVO agentMsgVO) {
        sendToWebSocket(clusterId, agentMsgVO.getKey(), JsonHelper.marshalByJackson(agentMsgVO));
    }

    private void sendToWebSocket(Long clusterId, String key, String textMessage) {
        webSocketHelper.sendByGroup(CLUSTER + clusterId, key, textMessage);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Send to webSocket: cluster: {}, key: {}, textMessage: {}", clusterId, key, LogUtil.cutOutString(textMessage, 200));
        }
    }

    @Override
    public void deletePod(String podName, String namespace, Long clusterId) {
        AgentMsgVO msg = new AgentMsgVO();
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId));
        msg.setPayload(JsonHelper.marshalByJackson(new DeletePodVO(podName, namespace)));
        msg.setType(HelmType.DELETE_POD.toValue());
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void unloadCertManager(Long clusterId, String certManagerReleaseName, String namespace) {
        AgentMsgVO msg = new AgentMsgVO();
        msg.setKey(String.format(KEY_FORMAT, clusterId, certManagerReleaseName));
        msg.setType(HelmType.CERT_MANAGER_UNINSTALL.toValue());
        HashMap<String, String> payLoad = new HashMap<>();
        payLoad.put(CertManagerConstants.RELEASE_NAME, certManagerReleaseName);
        payLoad.put(CertManagerConstants.NAMESPACE, namespace);
        msg.setPayload(gson.toJson(payLoad));
        sendToWebSocket(clusterId, msg);
    }

    @Override
    public void scanCluster(Long clusterId, Long recordId, @Nullable String namespace) {
        LOGGER.info("Polaris: start to send the polaris scan message...");
        AgentMsgVO msg = new AgentMsgVO();
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId));
        ClusterPolarisScanningVO clusterPolarisScanningVO = new ClusterPolarisScanningVO(Objects.requireNonNull(recordId), namespace);
        msg.setPayload(JsonHelper.marshalByJackson(clusterPolarisScanningVO));
        msg.setType(HelmType.POLARIS_SCAN_CLUSTER.toValue());
        sendToWebSocket(clusterId, msg);
        LOGGER.info("Polaris: successfully sent the polaris scan message...");
    }

    @Override
    public void sendChartMuseumAuthentication(Long clusterId, ConfigVO configVO) {
        LOGGER.debug("sendChartMuseumAuthentication. cluster id: {}", clusterId);
        AgentMsgVO msg = new AgentMsgVO();
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId));
        msg.setType(HelmType.CHART_MUSEUM_AUTHENTICATION.toValue());
        msg.setPayload(JsonHelper.marshalByJackson(configVO));
        sendToWebSocket(clusterId, msg);
        LOGGER.debug("Finished to sendChartMuseumAuthentication. cluster id {}", clusterId);
    }

    @Override
    public void sendRestartAgent(Long clusterId) {
        AgentMsgVO msg = new AgentMsgVO();
        msg.setType(RESTART_AGENT);
        msg.setKey(String.format(CLUSTER_FORMAT, clusterId));
        sendToWebSocket(clusterId, msg);
    }
}
