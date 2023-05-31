package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiTplVulnScanConfigDTO;

/**
 * 流水线模板ci 漏洞扫描配置信息表(CiTplVulnScanConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 09:37:23
 */
public interface CiTplVulnScanConfigService {

    CiTplVulnScanConfigDTO queryByStepId(Long stepId);

    void baseCreate(CiTplVulnScanConfigDTO ciTplVulnScanConfigDTO);

    void deleteByTemplateStepId(Long stepId);
}

