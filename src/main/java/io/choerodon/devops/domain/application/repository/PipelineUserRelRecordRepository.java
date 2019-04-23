package io.choerodon.devops.domain.application.repository;

import io.choerodon.devops.domain.application.entity.PipelineUserRecordRelE;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:24 2019/4/9
 * Description:
 */
public interface PipelineUserRelRecordRepository {
    PipelineUserRecordRelE create(PipelineUserRecordRelE recordRelE);

    List<PipelineUserRecordRelE> queryByRecordId(Long pipelineRecordId, Long stageRecordId, Long taskRecordId);
}
