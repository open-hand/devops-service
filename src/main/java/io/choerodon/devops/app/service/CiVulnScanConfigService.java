package io.choerodon.devops.app.service;

import java.util.Set;

import io.choerodon.devops.infra.dto.CiVulnScanConfigDTO;

/**
 * ci 漏洞扫描配置信息表(CiVulnScanConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 09:37:11
 */
public interface CiVulnScanConfigService {

    void baseCreate(CiVulnScanConfigDTO vulnScanConfig);

    CiVulnScanConfigDTO queryByStepId(Long stepId);

    void batchDeleteByStepIds(Set<Long> stepIds);
}

