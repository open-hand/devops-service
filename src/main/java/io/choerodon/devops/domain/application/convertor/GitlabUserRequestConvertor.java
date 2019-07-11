package io.choerodon.devops.domain.application.convertor;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabUserRequestDTO;
<<<<<<< HEAD
import io.choerodon.devops.app.eventhandler.payload.GitlabUserPayload;
=======
import io.choerodon.devops.domain.application.event.GitlabUserEvent;
>>>>>>> [IMP] applicationController重构

/**
 * Created by Zenger on 2018/3/29.
 */
@Component
public class GitlabUserRequestConvertor implements ConvertorI<GitlabUserPayload, Object, GitlabUserRequestDTO> {

    @Override
    public GitlabUserPayload dtoToEntity(GitlabUserRequestDTO gitlabUserRequestDTO) {
        GitlabUserPayload gitlabUserPayload = new GitlabUserPayload();
        gitlabUserPayload.setCanCreateGroup(gitlabUserRequestDTO.getCanCreateGroup());
        gitlabUserPayload.setConfirmedAt(gitlabUserRequestDTO.getConfirmedAt());
        gitlabUserPayload.setEmail(gitlabUserRequestDTO.getEmail());
        gitlabUserPayload.setExternUid(gitlabUserRequestDTO.getExternUid());
        gitlabUserPayload.setName(gitlabUserRequestDTO.getName());
        gitlabUserPayload.setProjectsLimit(gitlabUserRequestDTO.getProjectsLimit());
        gitlabUserPayload.setProvider(gitlabUserRequestDTO.getProvider());
        gitlabUserPayload.setSkipConfirmation(gitlabUserRequestDTO.getSkipConfirmation());
        gitlabUserPayload.setUsername(gitlabUserRequestDTO.getUsername());
        return gitlabUserPayload;
    }
}
