package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsCdJobRecordMapper extends BaseMapper<DevopsCdJobRecordDTO> {

    DevopsCdJobRecordDTO queryFirstByStageRecordId(@Param("stageRecordId") Long stageRecordId);
}
