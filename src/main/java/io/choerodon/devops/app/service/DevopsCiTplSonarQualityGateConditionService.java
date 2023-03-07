package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateConditionVO;

public interface DevopsCiTplSonarQualityGateConditionService {
    List<DevopsCiSonarQualityGateConditionVO> listByGateId(Long gateId);
}
