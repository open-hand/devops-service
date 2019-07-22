package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineUserRecordRelationshipDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:24 2019/4/9
 * Description:
 */
public interface PipelineUserRelRecordRepository {
    PipelineUserRecordRelationshipDTO baseCreate(PipelineUserRecordRelationshipDTO pipelineUserRecordRelationshipDTO);

    List<PipelineUserRecordRelationshipDTO> baseListByOptions(Long pipelineRecordId, Long stageRecordId, Long taskRecordId);

    void baseDelete(Long pipelineRecordId, Long stageRecordId, Long taskRecordId);
}
