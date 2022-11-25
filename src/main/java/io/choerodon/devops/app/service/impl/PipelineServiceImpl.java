package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.PipelineVO;
import io.choerodon.devops.api.vo.cd.PipelineStageVO;
import io.choerodon.devops.app.eventhandler.cd.AbstractCdJobHandler;
import io.choerodon.devops.app.eventhandler.cd.CdJobOperator;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.cd.PipelineStatusEnum;
import io.choerodon.devops.infra.enums.cd.PipelineTriggerTypeEnum;
import io.choerodon.devops.infra.mapper.PipelineMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线表(Pipeline)应用服务
 *
 * @author
 * @since 2022-11-24 15:50:13
 */
@Service
public class PipelineServiceImpl implements PipelineService {
    private static final String DEVOPS_SAVE_PIPELINE_FAILED = "devops.save.pipeline.failed";
    private static final String DEVOPS_ENABLE_PIPELINE_FAILED = "devops.enable.pipeline.failed";
    private static final String DEVOPS_DISABLE_PIPELINE_FAILED = "devops.disable.pipeline.failed";
    private static final String DEVOPS_PIPELINE_STATUS_IS_DISABLE = "devops.pipeline.status.is.disable";

    @Autowired
    private PipelineMapper pipelineMapper;
    @Autowired
    private PipelineStageService pipelineStageService;
    @Autowired
    private PipelineVersionService pipelineVersionService;
    @Autowired
    private PipelineJobService pipelineJobService;
    @Autowired
    private PipelineRecordService pipelineRecordService;
    @Autowired
    private PipelineStageRecordService pipelineStageRecordService;
    @Autowired
    private PipelineJobRecordService pipelineJobRecordService;
    @Autowired
    private CdJobOperator cdJobOperator;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineDTO pipelineDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineMapper, pipelineDTO, DEVOPS_SAVE_PIPELINE_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(PipelineDTO pipelineDTO) {
        pipelineMapper.updateByPrimaryKeySelective(pipelineDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDeleteById(Long id) {
        pipelineMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PipelineDTO baseQueryById(Long id) {
        return pipelineMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineDTO create(Long projectId, PipelineVO pipelineVO) {

        //
        PipelineDTO pipelineDTO = ConvertUtils.convertObject(pipelineVO, PipelineDTO.class);

        // 初始化令牌
        if (StringUtils.isEmpty(pipelineVO.getToken())) {
            pipelineDTO.setToken(GenerateUUID.generateUUID());
        }
        baseCreate(pipelineDTO);

        savePipelieVersion(projectId, pipelineVO, pipelineDTO);
        return pipelineDTO;
    }

    private void savePipelieVersion(Long projectId, PipelineVO pipelineVO, PipelineDTO pipelineDTO) {
        PipelineVersionDTO pipelineVersionDTO = pipelineVersionService.createByPipelineId(pipelineDTO.getId());
        pipelineDTO.setEffectVersionId(pipelineVersionDTO.getId());
        List<PipelineStageVO> stageList = pipelineVO.getStageList();
        if (CollectionUtils.isEmpty(stageList)) {
            throw new CommonException("devops.pipeline.stage.is.empty");
        }
        stageList.forEach(stage -> {
            pipelineStageService.saveStage(projectId, pipelineDTO.getId(), pipelineVersionDTO.getId(), stage);
        });
        baseUpdate(pipelineDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        if (Boolean.FALSE.equals(pipelineDTO.getEnable())) {
            pipelineDTO.setEnable(true);
            MapperUtil.resultJudgedInsertSelective(pipelineMapper, pipelineDTO, DEVOPS_ENABLE_PIPELINE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        if (Boolean.TRUE.equals(pipelineDTO.getEnable())) {
            pipelineDTO.setEnable(false);
            MapperUtil.resultJudgedInsertSelective(pipelineMapper, pipelineDTO, DEVOPS_DISABLE_PIPELINE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId, Long id) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        if (pipelineDTO == null) {
            return;
        }
        CommonExAssertUtil.assertTrue(projectId.equals(pipelineDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
        // 1. 删除流水线定义
        // 删除任务、任务配置
        pipelineJobService.deleteByPipelineId(id);
        // 删除阶段
        pipelineStageService.deleteByPipelineId(id);
        //删除版本
        pipelineVersionService.deleteByPipelineId(id);
        // 删除流水线
        baseDeleteById(id);

        // 2. 删除流水线执行记录
        // 删除任务记录
        pipelineJobRecordService.deleteByPipelineId(id);
        // 删除阶段记录
        pipelineStageRecordService.deleteByPipelineId(id);
        // 删除流水线记录
        pipelineRecordService.deleteByPipelineId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long projectId, Long id, PipelineVO pipelineVO) {
        //
        PipelineDTO pipelineDTO = ConvertUtils.convertObject(pipelineVO, PipelineDTO.class);

        savePipelieVersion(projectId, pipelineVO, pipelineDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineRecordDTO execute(Long projectId,
                                     Long id,
                                     PipelineTriggerTypeEnum triggerType,
                                     Map<String, Object> params) {
        PipelineDTO pipelineDTO = baseQueryById(id);
        if (Boolean.FALSE.equals(pipelineDTO.getEnable())) {
            throw new CommonException(DEVOPS_PIPELINE_STATUS_IS_DISABLE);
        }
        Long effectVersionId = pipelineDTO.getEffectVersionId();

        // 初始化流水线记录
        PipelineRecordDTO pipelineRecordDTO = new PipelineRecordDTO();
        pipelineRecordDTO.setTriggerType(triggerType.value());
        pipelineRecordDTO.setStatus(PipelineStatusEnum.CREATED.value());
        pipelineRecordDTO.setPipelineId(id);
        pipelineRecordService.baseCreate(pipelineRecordDTO);

        Long pipelineRecordId = pipelineRecordDTO.getId();

        //初始化阶段
        List<PipelineStageDTO> pipelineStageDTOS = pipelineStageService.listByVersionId(effectVersionId);
        pipelineStageDTOS.forEach(stage -> {
            Long stageId = stage.getId();
            PipelineStageRecordDTO pipelineStageRecordDTO = new PipelineStageRecordDTO(id,
                    stageId,
                    pipelineRecordId,
                    stage.getSequence(),
                    PipelineStatusEnum.CREATED.value());
            pipelineStageRecordService.baseCreate(pipelineStageRecordDTO);
            Long stageRecordId = pipelineStageRecordDTO.getId();
            // 初始化任务记录
            List<PipelineJobDTO> pipelineJobDTOS = pipelineJobService.listByStageId(stageId);
            pipelineJobDTOS.forEach(job -> {
                Long jobId = job.getId();
                PipelineJobRecordDTO pipelineJobRecordDTO = new PipelineJobRecordDTO(id,
                        jobId,
                        stageRecordId,
                        PipelineStatusEnum.CREATED.value());
                pipelineJobRecordService.baseCreate(pipelineJobRecordDTO);

                AbstractCdJobHandler handler = cdJobOperator.getHandlerOrThrowE(job.getType());
                handler.initAdditionalRecordInfo(id, job, pipelineJobRecordDTO);
            });
        });

        return pipelineRecordDTO;
    }
}

