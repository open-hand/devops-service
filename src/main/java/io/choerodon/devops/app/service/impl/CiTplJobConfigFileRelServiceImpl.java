package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.CiTplJobConfigFileRelService;
import io.choerodon.devops.infra.mapper.CiTplJobConfigFileRelMapper;

/**
 * CI任务模板配置文件关联表(CiTplJobConfigFileRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-02-16 15:50:17
 */
@Service
public class CiTplJobConfigFileRelServiceImpl implements CiTplJobConfigFileRelService {
    @Autowired
    private CiTplJobConfigFileRelMapper ciTplJobConfigFileRelMapper;
}

