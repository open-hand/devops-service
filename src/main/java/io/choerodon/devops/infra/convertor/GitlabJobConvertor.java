package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabJobDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabJobE;
import io.choerodon.devops.infra.dto.gitlab.JobDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


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
