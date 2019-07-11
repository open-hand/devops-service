package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabPipelineE;
import io.choerodon.devops.infra.dto.gitlab.PipelineDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/GitlabPipelineConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.gitlab.GitlabPipelineE;
import io.choerodon.devops.infra.dataobject.gitlab.PipelineDO;

>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/GitlabPipelineConvertor.java
/**
 * Created by Zenger on 2018/4/3.
 */
@Component
public class GitlabPipelineConvertor implements ConvertorI<GitlabPipelineE, PipelineDO, Object> {

    @Override
    public GitlabPipelineE doToEntity(PipelineDO pipelineDO) {
        GitlabPipelineE gitlabPipelineE = new GitlabPipelineE();
        BeanUtils.copyProperties(pipelineDO, gitlabPipelineE);
        if (pipelineDO.getUser() != null) {
            gitlabPipelineE.initUser(pipelineDO.getUser().getId(), pipelineDO.getUser().getUsername());
        }
        return gitlabPipelineE;
    }
}
