//package io.choerodon.devops.app.service.impl;
//
//import java.util.List;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.Assert;
//import org.springframework.util.CollectionUtils;
//
//import io.choerodon.core.exception.CommonException;
//import io.choerodon.devops.app.service.DevopsCdStageRecordService;
//import io.choerodon.devops.infra.constant.PipelineCheckConstant;
//import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
//import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;
//import io.choerodon.devops.infra.enums.PipelineStatus;
//import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;
//import io.choerodon.devops.infra.mapper.DevopsCdStageRecordMapper;
//
///**
// * 〈功能简述〉
// * 〈〉
// *
// * @author wanghao
// * @since 2020/7/2 11:07
// */
//@Service
//public class DevopsCdStageRecordServiceImpl implements DevopsCdStageRecordService {
//    public static final Logger LOGGER = LoggerFactory.getLogger(DevopsCdStageRecordServiceImpl.class);
//
//    private static final String DEVOPS_SAVE_STAGE_RECORD_FAILED = "devops.save.stage.record.failed";
//    private static final String DEVOPS_UPDATE_STAGE_RECORD_FAILED = "devops.update.stage.record.failed";
//
//    @Autowired
//    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;
//    @Autowired
//    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;
//    @Autowired
//    private DevopsCdJobRecordService devopsCdJobRecordService;
//
//    @Override
//    @Transactional
//    public void save(DevopsCdStageRecordDTO devopsCdStageRecordDTO) {
//        if (devopsCdStageRecordMapper.insert(devopsCdStageRecordDTO) != 1) {
//            throw new CommonException(DEVOPS_SAVE_STAGE_RECORD_FAILED);
//        }
//    }
//
//    @Override
//    public List<DevopsCdStageRecordDTO> queryByPipelineRecordId(Long pipelineRecordId) {
//        DevopsCdStageRecordDTO recordDTO = new DevopsCdStageRecordDTO();
//        recordDTO.setPipelineRecordId(pipelineRecordId);
//        return devopsCdStageRecordMapper.select(recordDTO);
//    }
//
//    @Override
//    public DevopsCdStageRecordDTO queryFirstByPipelineRecordId(Long pipelineRecordId) {
//        return devopsCdStageRecordMapper.queryFirstByPipelineRecordId(pipelineRecordId);
//    }
//
//    @Override
//    @Transactional
//    public void update(DevopsCdStageRecordDTO devopsCdStageRecord) {
//        if (devopsCdStageRecordMapper.updateByPrimaryKeySelective(devopsCdStageRecord) != 1) {
//            throw new CommonException(DEVOPS_UPDATE_STAGE_RECORD_FAILED);
//        }
//    }
//
//    @Override
//    @Transactional
//    public void updateStatusById(Long stageRecordId, String status) {
//        DevopsCdStageRecordDTO recordDTO = devopsCdStageRecordMapper.selectByPrimaryKey(stageRecordId);
//        // 已取消的阶段 不能更新为成功、失败状态
//        if (recordDTO.getStatus().equals(PipelineStatus.CANCELED.toValue())
//                && (status.equals(PipelineStatus.FAILED.toValue())
//                || status.equals(PipelineStatus.SUCCESS.toValue()))) {
//            LOGGER.info("cancel stage can not update status!! stage record Id {}", recordDTO.getId());
//            return;
//        }
//
//        recordDTO.setStatus(status);
//        if (devopsCdStageRecordMapper.updateByPrimaryKey(recordDTO) != 1) {
//            throw new CommonException(DEVOPS_UPDATE_STAGE_RECORD_FAILED);
//        }
//    }
//
//    @Override
//    public DevopsCdStageRecordDTO queryById(Long id) {
//        Assert.notNull(id, PipelineCheckConstant.DEVOPS_STAGE_RECORD_ID_IS_NULL);
//        return devopsCdStageRecordMapper.selectByPrimaryKey(id);
//    }
//
//    @Override
//    @Transactional
//    public void updateStageStatusFailed(Long stageRecordId) {
//        DevopsCdStageRecordDTO devopsCdStageRecordDTO = queryById(stageRecordId);
//        devopsCdStageRecordDTO.setStatus(PipelineStatus.FAILED.toValue());
//        update(devopsCdStageRecordDTO);
//    }
//
//    @Override
//    @Transactional
//    public void deleteByPipelineRecordId(Long pipelineRecordId) {
//        Assert.notNull(pipelineRecordId, PipelineCheckConstant.DEVOPS_STAGE_RECORD_ID_IS_NULL);
//        DevopsCdStageRecordDTO devopsCdStageRecordDTO = new DevopsCdStageRecordDTO();
//        devopsCdStageRecordDTO.setPipelineRecordId(pipelineRecordId);
//        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordMapper.select(devopsCdStageRecordDTO);
//        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
//            // 删除cd job记录
//            devopsCdStageRecordDTOS.forEach(cdStageRecordDTO -> {
//                DevopsCdJobRecordDTO devopsCdJobRecordDTO = new DevopsCdJobRecordDTO();
//                devopsCdJobRecordDTO.setStageRecordId(cdStageRecordDTO.getId());
//                devopsCdJobRecordMapper.delete(devopsCdJobRecordDTO);
//            });
//        }
//        //删除cd stage 记录
//        devopsCdStageRecordMapper.delete(devopsCdStageRecordDTO);
//    }
//
//    @Override
//    @Transactional
//    public void updateStageStatusStop(Long stageRecordId) {
//        Assert.notNull(stageRecordId, PipelineCheckConstant.DEVOPS_STAGE_RECORD_ID_IS_NULL);
//        // 更新阶段状态为stop
//        DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordMapper.selectByPrimaryKey(stageRecordId);
//        devopsCdStageRecordDTO.setStatus(PipelineStatus.STOP.toValue());
//        update(devopsCdStageRecordDTO);
//
//        // 更新阶段中的所有任务状态为stop
//        devopsCdJobRecordService.updateJobStatusStopByStageRecordId(stageRecordId);
//    }
//
//    @Override
//    public List<DevopsCdStageRecordDTO> queryStageWithPipelineRecordIdAndStatus(Long pipelineRecordId, String status) {
//        Assert.notNull(pipelineRecordId, PipelineCheckConstant.DEVOPS_PIPELINE_RECORD_ID_IS_NULL);
//        Assert.notNull(status, PipelineCheckConstant.DEVOPS_STAGE_STATUS_IS_NULL);
//
//        DevopsCdStageRecordDTO devopsCdStageRecordDTO = new DevopsCdStageRecordDTO();
//        devopsCdStageRecordDTO.setPipelineRecordId(pipelineRecordId);
//        devopsCdStageRecordDTO.setStatus(status);
//
//        return devopsCdStageRecordMapper.select(devopsCdStageRecordDTO);
//    }
//}
