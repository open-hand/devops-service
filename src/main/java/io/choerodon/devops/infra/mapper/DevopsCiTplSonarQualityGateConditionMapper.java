package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiTplSonarQualityGateConditionDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiTplSonarQualityGateConditionMapper extends BaseMapper<DevopsCiTplSonarQualityGateConditionDTO> {

    List<DevopsCiTplSonarQualityGateConditionDTO> listByGateId(@Param("gateId") Long gateId);
}
