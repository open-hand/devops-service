package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdAuditRecordService;
import io.choerodon.devops.app.service.DevopsCdPipelineRecordService;
import io.choerodon.devops.app.service.DevopsCdStageRecordService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCdJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCdStageRecordDTO;
import io.choerodon.devops.infra.enums.PipelineStatus;
import io.choerodon.devops.infra.mapper.DevopsCdJobRecordMapper;
import io.choerodon.devops.infra.mapper.DevopsCdStageRecordMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 11:07
 */
@Service
public class DevopsCdStageRecordServiceImpl implements DevopsCdStageRecordService {

    private static final String SAVE_STAGE_RECORD_FAILED = "save.stage.record.failed";
    private static final String UPDATE_STAGE_RECORD_FAILED = "update.stage.record.failed";

    @Autowired
    private DevopsCdStageRecordMapper devopsCdStageRecordMapper;
    @Autowired
    private DevopsCdJobRecordMapper devopsCdJobRecordMapper;

    @Autowired
    @Lazy
    private DevopsCdPipelineRecordService devopsCdPipelineRecordService;

    @Autowired
    private DevopsCdAuditRecordService devopsCdAuditRecordService;


    @Override
    @Transactional
    public void save(DevopsCdStageRecordDTO devopsCdStageRecordDTO) {
        if (devopsCdStageRecordMapper.insert(devopsCdStageRecordDTO) != 1) {
            throw new CommonException(SAVE_STAGE_RECORD_FAILED);
        }
    }

    @Override
    public List<DevopsCdStageRecordDTO> queryByPipelineRecordId(Long pipelineRecordId) {
        DevopsCdStageRecordDTO recordDTO = new DevopsCdStageRecordDTO();
        recordDTO.setPipelineRecordId(pipelineRecordId);
        return devopsCdStageRecordMapper.select(recordDTO);
    }

    @Override
    public DevopsCdStageRecordDTO queryFirstByPipelineRecordId(Long pipelineRecordId) {
        return devopsCdStageRecordMapper.queryFirstByPipelineRecordId(pipelineRecordId);
    }

    @Override
    @Transactional
    public void update(DevopsCdStageRecordDTO devopsCdStageRecord) {
        if (devopsCdStageRecordMapper.updateByPrimaryKeySelective(devopsCdStageRecord) != 1) {
            throw new CommonException(UPDATE_STAGE_RECORD_FAILED);
        }
    }

    @Override
    @Transactional
    public void updateStatusById(Long stageRecordId, String status) {
        DevopsCdStageRecordDTO recordDTO = devopsCdStageRecordMapper.selectByPrimaryKey(stageRecordId);
        recordDTO.setStatus(status);
        if (devopsCdStageRecordMapper.updateByPrimaryKey(recordDTO) != 1) {
            throw new CommonException(UPDATE_STAGE_RECORD_FAILED);
        }
    }

    @Override
    public DevopsCdStageRecordDTO queryById(Long id) {
        Assert.notNull(id, PipelineCheckConstant.ERROR_STAGE_RECORD_ID_IS_NULL);
        return devopsCdStageRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void updateStageStatusFailed(Long stageRecordId) {
        DevopsCdStageRecordDTO devopsCdStageRecordDTO = queryById(stageRecordId);
        devopsCdStageRecordDTO.setStatus(PipelineStatus.FAILED.toValue());
        update(devopsCdStageRecordDTO);
    }

    @Override
    @Transactional
    public void updateStageStatusNotAudit(Long pipelineRecordId, Long stageRecordId) {
        // 更新阶段状态为待审核
        DevopsCdStageRecordDTO devopsCdStageRecordDTO = devopsCdStageRecordMapper.selectByPrimaryKey(stageRecordId);
        devopsCdStageRecordDTO.setStatus(PipelineStatus.NOT_AUDIT.toValue());
        update(devopsCdStageRecordDTO);
        // 更新流水线状态为待审核
        devopsCdPipelineRecordService.updateStatusById(pipelineRecordId, PipelineStatus.NOT_AUDIT.toValue());
        // 通知审核人员
        devopsCdAuditRecordService.sendStageAuditMessage(devopsCdStageRecordDTO);
    }

    @Override
    @Transactional
    public void deleteByPipelineRecordId(Long pipelineRecordId) {
        Assert.notNull(pipelineRecordId, PipelineCheckConstant.ERROR_STAGE_RECORD_ID_IS_NULL);
        DevopsCdStageRecordDTO devopsCdStageRecordDTO = new DevopsCdStageRecordDTO();
        devopsCdStageRecordDTO.setPipelineRecordId(pipelineRecordId);
        List<DevopsCdStageRecordDTO> devopsCdStageRecordDTOS = devopsCdStageRecordMapper.select(devopsCdStageRecordDTO);
        if (!CollectionUtils.isEmpty(devopsCdStageRecordDTOS)) {
            // 删除cd job记录
            devopsCdStageRecordDTOS.forEach(cdStageRecordDTO -> {
                DevopsCdJobRecordDTO devopsCdJobRecordDTO = new DevopsCdJobRecordDTO();
                devopsCdJobRecordDTO.setStageRecordId(cdStageRecordDTO.getId());
                devopsCdJobRecordMapper.delete(devopsCdJobRecordDTO);
            });
        }
        //删除cd stage 记录
        devopsCdStageRecordMapper.delete(devopsCdStageRecordDTO);
    }
}
