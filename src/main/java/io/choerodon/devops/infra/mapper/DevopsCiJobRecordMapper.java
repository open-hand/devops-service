package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:22
 */
public interface DevopsCiJobRecordMapper extends BaseMapper<DevopsCiJobRecordDTO> {

    void batchInert(@Param("devopsCiJobRecordDTOS") List<DevopsCiJobRecordDTO> devopsCiJobRecordDTOS);

    DevopsCiPipelineRecordDTO queryLatestedPipelineRecord(@Param("pipelineId") Long pipelineId);

    void updateApiTestTaskRecordInfo(@Param("token") String token, @Param("gitlabJobId") Long gitlabJobId, @Param("configId") Long configId, @Param("apiTestTaskRecordId") Long apiTestTaskRecordId);
}
