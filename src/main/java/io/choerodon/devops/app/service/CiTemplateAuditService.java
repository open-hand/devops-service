package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
import io.choerodon.devops.api.vo.pipeline.CiTemplateAuditConfigVO;

/**
 * ci 人工卡点模板配置表(CiTemplateAudit)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-02 14:39:47
 */
public interface CiTemplateAuditService {

    CiTemplateAuditConfigVO queryConfigWithUsersById(Long id);

    CiAuditConfigVO queryConfigWithUserDetailsById(Long id);
}

