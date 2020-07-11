package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCdAuditRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;

/**
 * @author scp
 * @date 2020/7/3
 * @description
 */
public interface DevopsCdAuditRecordService {

    List<DevopsCdAuditRecordDTO> queryByStageRecordId(Long stageRecordId);

    List<DevopsCdAuditRecordDTO> queryByJobRecordId(Long jobRecordId);

    void sendStageAuditMessage(DevopsCdStageRecordDTO devopsCdStageRecordDTO);

    void sendJobAuditMessage(Long pipelineRecordId, DevopsCdJobRecordDTO devopsCdJobRecordDTO);

    void update(DevopsCdAuditRecordDTO devopsCdAuditRecordDTO);

    void save(DevopsCdAuditRecordDTO devopsCdAuditRecordDTO);

}
