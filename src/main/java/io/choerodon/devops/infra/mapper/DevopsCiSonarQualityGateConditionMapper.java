package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCiSonarQualityGateConditionDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiSonarQualityGateConditionMapper extends BaseMapper<DevopsCiSonarQualityGateConditionDTO> {
    List<DevopsCiSonarQualityGateConditionDTO> listByGateId(Long gateId);
}
