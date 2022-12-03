//package io.choerodon.devops.app.service;
//
//import java.util.List;
//
//import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
//
///**
// * @author scp
// * @date 2020/7/3
// * @description
// */
//public interface DevopsCdJobRecordService {
//
//    List<DevopsCdJobRecordDTO> queryByStageRecordId(Long stageRecordId);
//
//
//    /**
//     * 保存cd job执行记录
//     *
//     * @param devopsCdJobRecordDTO
//     */
//    void save(DevopsCdJobRecordDTO devopsCdJobRecordDTO);
//
//    DevopsCdJobRecordDTO queryFirstByStageRecordId(Long stageRecordId);
//
//    void update(DevopsCdJobRecordDTO devopsCdJobRecordDTO);
//
//    void updateStatusById(Long jobRecordId, String status);
//
//    void updateLogById(Long jobRecordId, StringBuilder log);
//
//    String getHostLogById(Long jobRecordId);
//
//    DevopsCdJobRecordDTO queryById(Long id);
//
//    void updateJobStatusFailed(Long jobRecordId, String log);
//
//    /**
//     * 1. 更新job状态为待审核
//     * 2. 更新stage状态为待审核
//     * 3. 更新pipeline状态为待审核
//     * 4. 通知审核人员审核
//     *
//     * @param pipelineRecordId
//     * @param stageRecordId
//     * @param jobRecordId
//     */
//    void updateJobStatusNotAudit(Long pipelineRecordId, Long stageRecordId, Long jobRecordId);
//
//    /**
//     * 重试cd job
//     *
//     * @param projectId
//     * @param pipelineRecordId
//     * @param stageRecordId
//     * @param jobRecordId
//     */
//    void retryCdJob(Long projectId, Long pipelineRecordId, Long stageRecordId, Long jobRecordId);
//
//    void updateJobStatusSuccess(Long jobRecordId);
//
//    /**
//     * 更新阶段下的所有job状态为stop
//     *
//     * @param stageRecordId
//     */
//    void updateJobStatusStopByStageRecordId(Long stageRecordId);
//
//
//    List<DevopsCdJobRecordDTO> queryJobWithStageRecordIdAndStatus(Long stageRecordId, String status);
//
//    DevopsCdJobRecordDTO queryByPipelineRecordIdAndJobName(Long pipelineRecordId, String deployJobName);
//}
