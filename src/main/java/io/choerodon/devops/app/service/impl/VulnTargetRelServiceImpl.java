package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.VulnTargetRelService;
import io.choerodon.devops.infra.mapper.VulnTargetRelMapper;

/**
 * 漏洞扫描对象关系表(VulnTargetRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
@Service
public class VulnTargetRelServiceImpl implements VulnTargetRelService {
    @Autowired
    private VulnTargetRelMapper vulnTargetRelMapper;
}

