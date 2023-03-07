package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.CiAuditUserDTO;

/**
 * ci 人工卡点审核人员表(CiAuditUser)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 11:40:41
 */
public interface CiAuditUserService {

    List<CiAuditUserDTO> listByAuditConfigId(Long configId);

    void batchCreateByConfigIdAndUserIds(Long configId, List<Long> cdAuditUserIds);

    void batchDeleteByConfigIds(List<Long> configIds);
}

