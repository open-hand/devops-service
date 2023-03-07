package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.pipeline.DevopsCiPipelineTriggerConfigVO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiPipelineTriggerConfigMapper extends BaseMapper<DevopsCiPipelineTriggerConfigDTO> {
    List<DevopsCiPipelineTriggerConfigDTO> listByJobIds(@Param("jobIds") List<Long> jobIds);

    void deleteByIds(@Param("ids") List<Long> ids);

    DevopsCiPipelineTriggerConfigVO queryConfigVoByIdWithVariables(Long configId);
}
