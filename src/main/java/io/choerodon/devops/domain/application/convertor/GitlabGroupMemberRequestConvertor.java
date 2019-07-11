package io.choerodon.devops.domain.application.convertor;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabGroupMemberDTO;
<<<<<<< HEAD
import io.choerodon.devops.app.eventhandler.payload.GitlabGroupMemberPayload;
=======
import io.choerodon.devops.domain.application.event.GitlabGroupMemberEvent;
>>>>>>> [IMP] applicationController重构

/**
 * Created by Zenger on 2018/3/30.
 */
@Component
public class GitlabGroupMemberRequestConvertor implements ConvertorI<GitlabGroupMemberPayload, Object, GitlabGroupMemberDTO> {

    @Override
    public GitlabGroupMemberPayload dtoToEntity(GitlabGroupMemberDTO gitlabGroupMemberDTO) {
        GitlabGroupMemberPayload gitlabGroupMemberPayload = new GitlabGroupMemberPayload();
        gitlabGroupMemberPayload.setProjectId(gitlabGroupMemberDTO.getResourceId());
        gitlabGroupMemberPayload.setRoleLabels(gitlabGroupMemberDTO.getRoleLabels());
        gitlabGroupMemberPayload.setUsername(gitlabGroupMemberDTO.getUsername());
        return gitlabGroupMemberPayload;
    }
}
