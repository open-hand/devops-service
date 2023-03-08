package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCiTemplatePipelineTriggerConfigVariableDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiTemplatePipelineTriggerConfigVariableMapper extends BaseMapper<DevopsCiTemplatePipelineTriggerConfigVariableDTO> {
    void deleteByConfigIds(@Param("triggerConfigIds") List<Long> triggerConfigIds);
}
