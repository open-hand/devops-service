package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.VulnScanTargetDTO;

/**
 * 漏洞扫描对象记录表(VulnScanTarget)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
public interface VulnScanTargetService {

    VulnScanTargetDTO baseCreate(Long scanRecordId, String target);
}

