package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsCiSonarQualityGateDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiSonarQualityGateMapper extends BaseMapper<DevopsCiSonarQualityGateDTO> {
    Boolean queryBlockByStepId(Long devopsCiSonarConfigId);
}
