//package io.choerodon.devops.infra.mapper;
//
//import java.util.List;
//
//import org.apache.ibatis.annotations.Param;
//
//import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
//import io.choerodon.mybatis.common.BaseMapper;
//
//public interface DevopsCdJobRecordMapper extends BaseMapper<DevopsCdJobRecordDTO> {
//
//    DevopsCdJobRecordDTO queryFirstByStageRecordId(@Param("stageRecordId") Long stageRecordId);
//
//    List<DevopsCdJobRecordDTO> queryRetryJob(@Param("stageRecordId") Long stageRecordId);
//
//    DevopsCdJobRecordDTO queryFirstJobByStageRecordIdAndStatus(@Param("stageRecordId") Long stageRecordId,
//                                                               @Param("status") String status);
//
//    List<DevopsCdJobRecordDTO> queryCreatedOrPendingOrRunning(@Param("stageRecordId") Long stageRecordId);
//
//    List<DevopsCdJobRecordDTO> listByIds(@Param("ids") List<Long> ids);
//
//    DevopsCdJobRecordDTO queryByPipelineRecordIdAndJobName(@Param("pipelineRecordId") Long pipelineRecordId,
//                                                           @Param("deployJobName") String deployJobName);
//}
