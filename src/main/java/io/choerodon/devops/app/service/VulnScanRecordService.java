package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.VulnScanRecordDTO;

/**
 * 漏洞扫描记录表(VulnScanRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
public interface VulnScanRecordService {

    VulnScanRecordDTO baseCreate(Long appServiceId, String branchName);

    void baseUpdate(VulnScanRecordDTO vulnScanRecordDTO);

}

