package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.PipelineUserRelationshipDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:48 2019/7/19
 * Description:
 */
public interface PipelineUserRelationshipService {
    void baseCreate(PipelineUserRelationshipDTO pipelineUserRelationShipDTO);

    List<PipelineUserRelationshipDTO> baseListByOptions(Long pipelineId, Long stageId, Long taskId);

    void baseDelete(PipelineUserRelationshipDTO pipelineUserRelationShipDTO);
}
