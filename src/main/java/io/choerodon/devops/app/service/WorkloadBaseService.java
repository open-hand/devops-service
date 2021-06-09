package io.choerodon.devops.app.service;

import io.choerodon.mybatis.domain.AuditDomain;

public interface WorkloadBaseService {

    AuditDomain selectByPrimaryKey(Long id);

    void checkExist(Long envId, String name);

    Long baseCreate(AuditDomain auditDomain);

    void baseUpdate(AuditDomain auditDomain);
}
