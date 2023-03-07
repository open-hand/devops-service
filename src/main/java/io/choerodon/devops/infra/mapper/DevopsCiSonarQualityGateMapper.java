package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;
import io.choerodon.devops.infra.dto.DevopsCiSonarQualityGateDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiSonarQualityGateMapper extends BaseMapper<DevopsCiSonarQualityGateDTO> {
    Boolean queryBlockByStepId(Long devopsCiSonarConfigId);

    void deleteByName(@Param("name") String name);

    Boolean qualityGateExistsByName(@Param("name") String name);

    DevopsCiSonarQualityGateVO queryByName(@Param("name") String name);
}
