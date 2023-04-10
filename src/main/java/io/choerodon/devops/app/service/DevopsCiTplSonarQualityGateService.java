package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;

public interface DevopsCiTplSonarQualityGateService {
    DevopsCiSonarQualityGateVO queryBySonarConfigId(Long sonarConfigId);
}
