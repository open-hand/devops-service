package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabCommitDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabCommitE;
import io.choerodon.devops.infra.dto.gitlab.CommitDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/GitlabCommitConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabCommitDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabCommitE;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDTO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/GitlabCommitConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/GitlabCommitConvertor.java
=======
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/GitlabCommitConvertor.java
>>>>>>> [IMP]修改后端结构

/**
 * Created by Zenger on 2018/4/3.
 */
@Component
public class GitlabCommitConvertor implements ConvertorI<GitlabCommitE, CommitDTO, GitlabCommitDTO> {

    @Override
    public GitlabCommitE doToEntity(CommitDTO commitDO) {
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
