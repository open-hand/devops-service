package io.choerodon.devops.app.service;

import java.util.List;

/**
 * ci 人工卡点用户审核记录表(CiAuditUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-03 10:32:19
 */
public interface CiAuditUserRecordService {

    void initAuditRecord(Long auditRecordId, List<Long> cdAuditUserIds);
}

