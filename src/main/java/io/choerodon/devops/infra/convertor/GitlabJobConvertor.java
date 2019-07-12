package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabJobDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabJobE;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/GitlabJobConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.GitlabJobDTO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabJobE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/GitlabJobConvertor.java
>>>>>>> [IMP]修改后端结构
import io.choerodon.devops.infra.dataobject.gitlab.JobDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/GitlabJobConvertor.java

/**
 * Created by Zenger on 2018/4/3.
 */
@Component
public class GitlabJobConvertor implements ConvertorI<GitlabJobE, JobDTO, GitlabJobDTO> {

    @Override
    public GitlabJobE doToEntity(JobDTO jobDTO) {
        GitlabJobE gitlabJobE = new GitlabJobE();
        BeanUtils.copyProperties(jobDTO, gitlabJobE);
        return gitlabJobE;
    }

    @Override
    public GitlabJobDTO entityToDto(GitlabJobE entity) {
        GitlabJobDTO gitlabJobDTO = new GitlabJobDTO();
        BeanUtils.copyProperties(entity, gitlabJobDTO);
        return gitlabJobDTO;
    }
}
