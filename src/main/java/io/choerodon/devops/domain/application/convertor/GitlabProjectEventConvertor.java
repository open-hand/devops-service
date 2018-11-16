package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.GitlabProjectEventDTO;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;

@Component
public class GitlabProjectEventConvertor implements ConvertorI<GitlabProjectPayload, Object, GitlabProjectEventDTO> {

    @Override
    public GitlabProjectPayload dtoToEntity(GitlabProjectEventDTO gitlabProjectEventDTO) {
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        BeanUtils.copyProperties(gitlabProjectEventDTO, gitlabProjectPayload);
        return gitlabProjectPayload;
    }
}