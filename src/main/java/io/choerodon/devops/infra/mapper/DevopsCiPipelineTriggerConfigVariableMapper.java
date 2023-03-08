package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigVariableDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiPipelineTriggerConfigVariableMapper extends BaseMapper<DevopsCiPipelineTriggerConfigVariableDTO> {
    void deleteByConfigIds(@Param("triggerConfigIds") List<Long> triggerConfigIds);
}
