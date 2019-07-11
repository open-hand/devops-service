package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabProjectEventDTO;
<<<<<<< HEAD
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;
=======
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;
>>>>>>> [IMP] applicationController重构

@Component
public class GitlabProjectEventConvertor implements ConvertorI<GitlabProjectPayload, Object, GitlabProjectEventDTO> {

    @Override
    public GitlabProjectPayload dtoToEntity(GitlabProjectEventDTO gitlabProjectEventDTO) {
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        BeanUtils.copyProperties(gitlabProjectEventDTO, gitlabProjectPayload);
        return gitlabProjectPayload;
    }
}