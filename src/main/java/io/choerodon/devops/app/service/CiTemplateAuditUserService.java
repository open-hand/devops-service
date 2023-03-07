package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.CiTemplateAuditUserDTO;

/**
 * ci 人工卡点审核人员表(CiTemplateAuditUser)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 14:39:57
 */
public interface CiTemplateAuditUserService {

    List<CiTemplateAuditUserDTO> listByConfigId(Long configId);
}

