package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabPipelineE;
import io.choerodon.devops.infra.dto.gitlab.GitlabPipelineDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/GitlabPipelineConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabPipelineE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/GitlabPipelineConvertor.java
>>>>>>> [IMP]修改后端结构
import io.choerodon.devops.infra.dataobject.gitlab.PipelineDO;

>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/GitlabPipelineConvertor.java
/**
 * Created by Zenger on 2018/4/3.
 */
@Component
public class GitlabPipelineConvertor implements ConvertorI<GitlabPipelineE, GitlabPipelineDTO, Object> {

    @Override
    public GitlabPipelineE doToEntity(GitlabPipelineDTO pipelineDTO) {
        GitlabPipelineE gitlabPipelineE = new GitlabPipelineE();
        BeanUtils.copyProperties(pipelineDTO, gitlabPipelineE);
        if (pipelineDTO.getUser() != null) {
            gitlabPipelineE.initUser(pipelineDTO.getUser().getId(), pipelineDTO.getUser().getUsername());
        }
        return gitlabPipelineE;
    }
}
