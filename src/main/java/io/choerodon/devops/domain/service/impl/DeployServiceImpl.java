package io.choerodon.devops.domain.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.GitConfigDTO;
import io.choerodon.devops.api.dto.GitEnvConfigDTO;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.repository.DevopsClusterRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.Payload;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.common.util.enums.HelmType;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;

/**
 * Created by younger on 2018/4/18.
 */
@Service
public class DeployServiceImpl implements DeployService {

    private static final String INIT_AGENT = "init_agent";
    private static final String DELETE_ENV = "delete_env";
    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private CommandSender commandSender;

    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsClusterRepository devopsClusterRepository;

    @Value("${services.helm.url}")
    private String helmUrl;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

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
    public void deploy(ApplicationE applicationE, ApplicationVersionE applicationVersionE, ApplicationInstanceE applicationInstanceE, DevopsEnvironmentE devopsEnvironmentE, String values, Long commandId) {
        Msg msg = new Msg();
        Payload payload = new Payload(
                devopsEnvironmentE.getCode(),
                helmUrl + applicationVersionE.getRepository(),
                applicationE.getCode(),
                applicationVersionE.getVersion(),
                values, applicationInstanceE.getCode());
        msg.setKey(String.format("cluster:%d.env:%s.envId:%d.release:%s",
                devopsEnvironmentE.getClusterE().getId(),
                devopsEnvironmentE.getCode(),
                devopsEnvironmentE.getId(),
                applicationInstanceE.getCode()));
        msg.setType(HelmType.HELM_RELEASE_PRE_UPGRADE.toValue());
        try {
            msg.setPayload(mapper.writeValueAsString(payload));
            msg.setCommandId(commandId);
        } catch (IOException e) {
            throw new CommonException("error.payload.error", e);
        }
        commandSender.sendMsg(msg);
    }

    @Override
    public void initCluster(Long clusterId) {
        List<DevopsEnvironmentE> devopsEnvironments = devopsEnvironmentRepository.listByClusterId(clusterId);
        GitConfigDTO gitConfigDTO = new GitConfigDTO();
        List<GitEnvConfigDTO> gitEnvConfigDTOS = new ArrayList<>();
        devopsEnvironments.stream().filter(devopsEnvironmentE -> devopsEnvironmentE.getGitlabEnvProjectId() != null).forEach(devopsEnvironmentE -> {
            ProjectE projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            String repoUrl = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());

            GitEnvConfigDTO gitEnvConfigDTO = new GitEnvConfigDTO();
            gitEnvConfigDTO.setEnvId(devopsEnvironmentE.getId());
            gitEnvConfigDTO.setGitRsaKey(devopsEnvironmentE.getEnvIdRsa());
            gitEnvConfigDTO.setGitUrl(repoUrl);
            gitEnvConfigDTO.setNamespace(devopsEnvironmentE.getCode());
            gitEnvConfigDTOS.add(gitEnvConfigDTO);
        });
        gitConfigDTO.setEnvs(gitEnvConfigDTOS);
        gitConfigDTO.setGitHost(gitlabSshUrl);
        Msg msg = new Msg();
        try {
            msg.setPayload(mapper.writeValueAsString(gitConfigDTO));
        } catch (IOException e) {
            throw new CommonException("read envId from agent session failed", e);
        }
        msg.setType(INIT_AGENT);
        msg.setKey(String.format("cluster:%s", clusterId
        ));
        commandSender.sendMsg(msg);
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
        msg.setKey(String.format("cluster:%s", clusterId));
        commandSender.sendMsg(msg);
    }
}
