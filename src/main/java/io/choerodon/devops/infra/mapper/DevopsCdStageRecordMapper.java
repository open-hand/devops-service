package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 11:08
 */
public interface DevopsCdStageRecordMapper extends BaseMapper<DevopsCdStageRecordDTO> {

    DevopsCdStageRecordDTO queryFirstByPipelineRecordId(@Param("pipelineRecordId") Long pipelineRecordId);
}
