package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;

public interface DevopsCiSonarQualityGateService {
    void deleteAll(Long id);

    void createGate(Long configId, DevopsCiSonarQualityGateVO sonarQualityGateVO);
    Boolean queryBlock(Long stepId);

    DevopsCiSonarQualityGateVO queryBySonarConfigId(Long id);
}
