package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.pipeline.DevopsCiTemplatePipelineTriggerConfigVO;
import io.choerodon.devops.infra.dto.DevopsCiTemplatePipelineTriggerConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiTemplatePipelineTriggerConfigMapper extends BaseMapper<DevopsCiTemplatePipelineTriggerConfigDTO> {
    DevopsCiTemplatePipelineTriggerConfigVO queryConfigVoById(@Param("configId") Long configId);
}
