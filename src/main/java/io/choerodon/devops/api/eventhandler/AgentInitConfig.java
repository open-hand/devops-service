package io.choerodon.devops.api.eventhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.GitConfigDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;
import io.choerodon.websocket.session.AgentConfigurer;
import io.choerodon.websocket.session.AgentSessionManager;
import io.choerodon.websocket.session.Session;
import io.choerodon.websocket.session.SessionListener;
import io.choerodon.websocket.tool.KeyParseTool;

@Component
public class AgentInitConfig implements AgentConfigurer {

    @Autowired
    CommandSender commandSender;
    @Autowired
    DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private IamRepository iamRepository;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String INIT_AGENT = "init_agent";

    @Override
    public void registerSessionListener(AgentSessionManager agentSessionManager) {
        AgentInitListener agentInitListener = new AgentInitListener();
        agentSessionManager.addListener(agentInitListener);
    }

    class AgentInitListener implements SessionListener {
        @Override
        public void onConnected(Session session) {
            try {
                String envId  =  KeyParseTool.parseKey(session.getRegisterKey()).get("envId");
                DevopsEnvironmentE env =  devopsEnvironmentRepository.queryById(Long.valueOf(envId));

                ProjectE projectE = iamRepository.queryIamProject(env.getProjectE().getId());
                Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
                String repoUrl = String.format("git@%s:%s-%s-gitops/%s.git",
                        gitlabSshUrl, organization.getCode(), projectE.getCode(), env.getCode());

                GitConfigDTO gitConfigDTO = new GitConfigDTO();
                gitConfigDTO.setGitUrl(repoUrl);
                gitConfigDTO.setSshKey(env.getEnvIdRsa());
                Msg msg = new Msg();
                msg.setPayload(OBJECT_MAPPER.writeValueAsString(gitConfigDTO));
                msg.setType(INIT_AGENT);
                msg.setKey(session.getRegisterKey());
                commandSender.sendMsg(msg);
            } catch (Exception e){
                throw new CommonException(e,"read envId from agent session failed");
            }
        }

        @Override
        public Session onClose(String s) {
            return null;
        }
    }


}
