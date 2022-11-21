package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateConditionVO;

public interface DevopsCiSonarQualityGateConditionService {
    void createConditions(Long gateId, String sonarGateId, List<DevopsCiSonarQualityGateConditionVO> sonarQualityGateConditionVOList);

    void deleteByGateId(Long gateId);

    List<DevopsCiSonarQualityGateConditionVO> listByGateId(Long id);
}
