package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.SonarAnalyseIssueSeverityDTO;

/**
 * 代码扫描问题分级统计表(SonarAnalyseIssueSeverity)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-09 09:34:44
 */
public interface SonarAnalyseIssueSeverityService {

    void baseCreate(SonarAnalyseIssueSeverityDTO sonarAnalyseIssueSeverityDTO);
}

