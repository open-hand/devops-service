package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabCommitDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabCommitE;
import io.choerodon.devops.infra.dto.gitlab.CommitDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Created by Zenger on 2018/4/3.
 */
@Component
public class GitlabCommitConvertor implements ConvertorI<GitlabCommitE, CommitDO, GitlabCommitDTO> {

    @Override
    public GitlabCommitE doToEntity(CommitDO commitDO) {
        GitlabCommitE gitlabCommitE = new GitlabCommitE();
        BeanUtils.copyProperties(commitDO, gitlabCommitE);
        return gitlabCommitE;
    }

    @Override
    public GitlabCommitDTO entityToDto(GitlabCommitE entity) {
        GitlabCommitDTO gitlabCommitDTO = new GitlabCommitDTO();
        BeanUtils.copyProperties(entity, gitlabCommitDTO);
        return gitlabCommitDTO;
    }
}
