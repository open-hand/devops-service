package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabProjectEventDTO;
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;

@Component
public class GitlabProjectEventConvertor implements ConvertorI<GitlabProjectPayload, Object, GitlabProjectEventDTO> {

    @Override
    public GitlabProjectPayload dtoToEntity(GitlabProjectEventDTO gitlabProjectEventDTO) {
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        BeanUtils.copyProperties(gitlabProjectEventDTO, gitlabProjectPayload);
        return gitlabProjectPayload;
    }
}