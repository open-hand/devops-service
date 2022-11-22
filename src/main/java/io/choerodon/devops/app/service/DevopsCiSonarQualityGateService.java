package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;
import io.choerodon.devops.api.vo.sonar.QualityGate;

public interface DevopsCiSonarQualityGateService {
    void deleteAll(String sonarProjectKey);

    void createGate(String sonarProjectKey, Long configId, DevopsCiSonarQualityGateVO sonarQualityGateVO);

    Boolean queryBlock(Long stepId);

    DevopsCiSonarQualityGateVO queryBySonarConfigId(Long configId);

    QualityGate createQualityGateOnSonarQube(String name);
}
