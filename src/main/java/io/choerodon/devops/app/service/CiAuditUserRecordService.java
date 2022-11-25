package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.CiAuditUserRecordDTO;

/**
 * ci 人工卡点用户审核记录表(CiAuditUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-03 10:32:19
 */
public interface CiAuditUserRecordService {

    void initAuditRecord(Long ciPipelineId, Long auditRecordId, List<Long> cdAuditUserIds);

    List<CiAuditUserRecordDTO> listByAuditRecordId(Long auditRecordId);

    void baseUpdate(CiAuditUserRecordDTO ciAuditUserRecordDTO);

    void deleteByCiPipelineId(Long pipelineId);
}

