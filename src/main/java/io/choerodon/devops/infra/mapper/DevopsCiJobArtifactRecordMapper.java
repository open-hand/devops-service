package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiJobArtifactRecordDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * @author zmf
 * @since 20-4-20
 */
public interface DevopsCiJobArtifactRecordMapper extends Mapper<DevopsCiJobArtifactRecordDTO> {
    DevopsCiJobArtifactRecordDTO queryByPipelineIdAndName(@Param("gitlabPipelineId") Long gitlabPipelineId, @Param("name") String name);
}
