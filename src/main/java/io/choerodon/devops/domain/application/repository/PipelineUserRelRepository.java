package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.api.vo.iam.entity.PipelineUserRelE;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:08 2019/4/8
 * Description:
 */
public interface PipelineUserRelRepository {
    void create(PipelineUserRelE pipelineUserRelE);

    List<PipelineUserRelE> listByOptions(Long pipelineId, Long stageId, Long taskId);

    void delete(PipelineUserRelE pipelineUserRelE);
}
