package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiJobArtifactRecordDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * @author zmf
 * @since 20-4-20
 */
public interface DevopsCiJobArtifactRecordMapper extends Mapper<DevopsCiJobArtifactRecordDTO> {
    DevopsCiJobArtifactRecordDTO queryByPipelineIdAndName(@Param("gitlabPipelineId") Long gitlabPipelineId, @Param("name") String name);

    /**
     * 根据gitlab流水线纪录id列表批量查询纪录
     *
     * @param gitlabPipelineIds gitlab流水线id列表
     * @return 查询结果列表
     */
    List<DevopsCiJobArtifactRecordDTO> listByGitlabPipelineIds(@Param("gitlabPipelineIds") List<Long> gitlabPipelineIds);

    void deleteByGitlabPipelineIds(@Param("gitlabPipelineIds") List<Long> gitlabPipelineIds);
}
