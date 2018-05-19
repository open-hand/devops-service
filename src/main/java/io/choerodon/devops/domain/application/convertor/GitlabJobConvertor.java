package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.GitlabJobDTO;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabJobE;
import io.choerodon.devops.infra.dataobject.gitlab.JobDO;

/**
 * Created by Zenger on 2018/4/3.
 */
@Component
public class GitlabJobConvertor implements ConvertorI<GitlabJobE, JobDO, GitlabJobDTO> {

    @Override
    public GitlabJobE doToEntity(JobDO jobDO) {
        GitlabJobE gitlabJobE = new GitlabJobE();
        BeanUtils.copyProperties(jobDO, gitlabJobE);
        return gitlabJobE;
    }

    @Override
    public GitlabJobDTO entityToDto(GitlabJobE entity) {
        GitlabJobDTO gitlabJobDTO = new GitlabJobDTO();
        BeanUtils.copyProperties(entity, gitlabJobDTO);
        return gitlabJobDTO;
    }
}
