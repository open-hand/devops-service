package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCdAuditRecordDTO;

/**
 * @author scp
 * @date 2020/7/3
 * @description
 */
public interface DevopsCdAuditRecordService {

    List<DevopsCdAuditRecordDTO> queryByStageRecordId(Long stageRecordId);

    List<DevopsCdAuditRecordDTO> queryByJobRecordId(Long jobRecordId);

}
