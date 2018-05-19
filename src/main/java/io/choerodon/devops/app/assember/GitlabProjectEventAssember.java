package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.GitlabProjectEventDTO;
import io.choerodon.devops.domain.application.event.GitlabProjectPayload;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabProjectEventAssember implements ConvertorI<GitlabProjectPayload, Object, GitlabProjectEventDTO> {

    @Override
    public GitlabProjectPayload dtoToEntity(GitlabProjectEventDTO gitlabProjectEventDTO) {
        GitlabProjectPayload gitlabProjectPayload = new GitlabProjectPayload();
        BeanUtils.copyProperties(gitlabProjectEventDTO, gitlabProjectPayload);
        return gitlabProjectPayload;
    }
}
