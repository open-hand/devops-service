package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.VulnScanTargetService;
import io.choerodon.devops.infra.mapper.VulnScanTargetMapper;

/**
 * 漏洞扫描对象记录表(VulnScanTarget)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
@Service
public class VulnScanTargetServiceImpl implements VulnScanTargetService {
    @Autowired
    private VulnScanTargetMapper vulnScanTargetMapper;
}

