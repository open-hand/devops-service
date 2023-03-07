package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiSonarQualityGateConditionDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiSonarQualityGateConditionMapper extends BaseMapper<DevopsCiSonarQualityGateConditionDTO> {
    List<DevopsCiSonarQualityGateConditionDTO> listByGateId(Long gateId);

    List<DevopsCiSonarQualityGateConditionDTO> listBySonarIds(@Param("conditionSonarIdList") List<String> conditionSonarIdList);

    void deleteByGateId(@Param("gateId") Long gateId);
}
