package io.choerodon.devops.domain.application.convertor;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.GitlabGroupMemberDTO;
import io.choerodon.devops.domain.application.event.GitlabGroupMemberEvent;

/**
 * Created by Zenger on 2018/3/30.
 */
@Component
public class GitlabGroupMemberRequestConvertor implements ConvertorI<GitlabGroupMemberEvent, Object, GitlabGroupMemberDTO> {

    @Override
    public GitlabGroupMemberEvent dtoToEntity(GitlabGroupMemberDTO gitlabGroupMemberDTO) {
        GitlabGroupMemberEvent gitlabGroupMemberEvent = new GitlabGroupMemberEvent();
        gitlabGroupMemberEvent.setProjectId(gitlabGroupMemberDTO.getResourceId());
        gitlabGroupMemberEvent.setRoleLabels(gitlabGroupMemberDTO.getRoleLabels());
        gitlabGroupMemberEvent.setUsername(gitlabGroupMemberDTO.getUsername());
        return gitlabGroupMemberEvent;
    }
}
