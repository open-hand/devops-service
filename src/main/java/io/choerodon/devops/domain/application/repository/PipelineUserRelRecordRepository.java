package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.PipelineUserRecordRelE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:24 2019/4/9
 * Description:
 */
public interface PipelineUserRelRecordRepository {
    PipelineUserRecordRelE baseCreatePipelineUserRecordRelationship(PipelineUserRecordRelE recordRelE);

    List<PipelineUserRecordRelE> queryPipelineUserRecordRelationshipByRecordId(Long pipelineRecordId, Long stageRecordId, Long taskRecordId);

    void deletePipelineUserRecordRelationshipByIds(Long pipelineRecordId, Long stageRecordId, Long taskRecordId);
}
