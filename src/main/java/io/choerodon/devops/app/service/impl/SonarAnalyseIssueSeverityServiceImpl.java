package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.SonarAnalyseIssueSeverityService;
import io.choerodon.devops.infra.dto.SonarAnalyseIssueSeverityDTO;
import io.choerodon.devops.infra.mapper.SonarAnalyseIssueSeverityMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 代码扫描问题分级统计表(SonarAnalyseIssueSeverity)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-09 09:34:44
 */
@Service
public class SonarAnalyseIssueSeverityServiceImpl implements SonarAnalyseIssueSeverityService {

    private static final String DEVOPS_SAVE_SONAR_ISSUE_SEVERITY_FAILED = "devops.save.sonar.issue.severity.failed";

    @Autowired
    private SonarAnalyseIssueSeverityMapper sonarAnalyseIssueSeverityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(SonarAnalyseIssueSeverityDTO sonarAnalyseIssueSeverityDTO) {
        MapperUtil.resultJudgedInsertSelective(sonarAnalyseIssueSeverityMapper, sonarAnalyseIssueSeverityDTO, DEVOPS_SAVE_SONAR_ISSUE_SEVERITY_FAILED);
    }
}

