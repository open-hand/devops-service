package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.VulnScanRecordService;
import io.choerodon.devops.infra.mapper.VulnScanRecordMapper;

/**
 * 漏洞扫描记录表(VulnScanRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
@Service
public class VulnScanRecordServiceImpl implements VulnScanRecordService {
    @Autowired
    private VulnScanRecordMapper vulnScanRecordMapper;
}

