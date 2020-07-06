package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 11:07
 */
public interface DevopsCdStageRecordService {
    /**
     * 保存流水线阶段记录
     * @param devopsCdStageRecordDTO
     */
    void save(DevopsCdStageRecordDTO devopsCdStageRecordDTO);


    List<DevopsCdStageRecordDTO> queryByPipelineRecordId(Long pipelineRecordId);

    void updateStatusById(Long stageRecordId, String status);
}
