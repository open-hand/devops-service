package io.choerodon.devops.domain.application.convertor;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.GitlabUserRequestDTO;
import io.choerodon.devops.domain.application.event.GitlabUserEvent;

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class GitlabUserRequestConvertor implements ConvertorI<GitlabUserEvent, Object, GitlabUserRequestDTO> {

    @Override
    public GitlabUserEvent dtoToEntity(GitlabUserRequestDTO gitlabUserRequestDTO) {
        GitlabUserEvent gitlabUserEvent = new GitlabUserEvent();
        gitlabUserEvent.setCanCreateGroup(gitlabUserRequestDTO.getCanCreateGroup());
        gitlabUserEvent.setConfirmedAt(gitlabUserRequestDTO.getConfirmedAt());
        gitlabUserEvent.setEmail(gitlabUserRequestDTO.getEmail());
        gitlabUserEvent.setExternUid(gitlabUserRequestDTO.getExternUid());
        gitlabUserEvent.setName(gitlabUserRequestDTO.getName());
        gitlabUserEvent.setProjectsLimit(gitlabUserRequestDTO.getProjectsLimit());
        gitlabUserEvent.setProvider(gitlabUserRequestDTO.getProvider());
        gitlabUserEvent.setSkipConfirmation(gitlabUserRequestDTO.getSkipConfirmation());
        gitlabUserEvent.setUsername(gitlabUserRequestDTO.getUsername());
        return gitlabUserEvent;
    }
}
