package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineUserRecordRelationshipDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:02 2019/7/19
 * Description:
 */
public interface PipelineUserRecordRelationshipService {
    PipelineUserRecordRelationshipDTO baseCreate(PipelineUserRecordRelationshipDTO pipelineUserRecordRelationshipDTO);

    List<PipelineUserRecordRelationshipDTO> baseListByOptions(Long pipelineRecordId, Long stageRecordId, Long taskRecordId);

    void baseDelete(Long pipelineRecordId, Long stageRecordId, Long taskRecordId);
}
