package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCiPipelineTriggerConfigMapper extends BaseMapper<DevopsCiPipelineTriggerConfigDTO> {
    DevopsCiPipelineTriggerConfigDTO queryGitlabProjectId(@Param("token") String token, @Param("configId") Long configId);

    List<DevopsCiPipelineTriggerConfigDTO> listByJobIds(@Param("jobIds") List<Long> jobIds);

    void deleteByIds(@Param("ids") List<Long> ids);
}
