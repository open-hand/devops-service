package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.SonarAnalyseUserRecordService;
import io.choerodon.devops.infra.mapper.SonarAnalyseUserRecordMapper;

/**
 * 代码扫描记录表(SonarAnalyseUserRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
@Service
public class SonarAnalyseUserRecordServiceImpl implements SonarAnalyseUserRecordService {
    @Autowired
    private SonarAnalyseUserRecordMapper sonarAnalyseUserRecordMapper;
}

