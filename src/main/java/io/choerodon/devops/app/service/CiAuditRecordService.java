package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.CiAuditResultVO;
import io.choerodon.devops.infra.dto.CiAuditRecordDTO;

/**
 * ci 人工卡点审核记录表(CiAuditRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-03 10:16:51
 */
public interface CiAuditRecordService {

    CiAuditRecordDTO queryByUniqueOption(Long appServiceId, Long gitlabPipelineId, String name);

    CiAuditRecordDTO queryByUniqueOptionForUpdate(Long appServiceId, Long gitlabPipelineId, String name);

    CiAuditRecordDTO baseCreate(CiAuditRecordDTO ciAuditRecordDTO);

    CiAuditResultVO queryAuditStatus(String token, Long gitlabPipelineId, String jobName);

    void sendJobAuditMessage(Long appServiceId, Long ciPipelineId, Long ciPipelineRecordId, Long gitlabPipelineId, String name, String stage);

    Boolean queryAuditRecordIsFinish(Long appServiceId, Long gitlabPipelineId, String jobName);

    void deleteByCiPipelineId(Long pipelineId);
}

