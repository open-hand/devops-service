package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;
import io.choerodon.devops.api.vo.sonar.QualityGate;
import io.choerodon.devops.api.vo.sonar.QualityGateResult;

public interface DevopsCiTplSonarQualityGateService {
    DevopsCiSonarQualityGateVO queryBySonarConfigId(Long sonarConfigId);
}
