package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.api.vo.cd.PipelineStageVO;
import io.choerodon.devops.app.eventhandler.cd.AbstractCdJobHandler;
import io.choerodon.devops.app.eventhandler.cd.CdJobOperator;
import io.choerodon.devops.app.service.PipelineStageService;
import io.choerodon.devops.infra.dto.PipelineStageDTO;
import io.choerodon.devops.infra.mapper.PipelineStageMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线阶段表(PipelineStage)应用服务
 *
 * @author
 * @since 2022-11-24 15:52:49
 */
@Service
public class PipelineStageServiceImpl implements PipelineStageService {

    private static final String DEVOPS_SAVE_STAGE_FAILED = "devops.save.stage.failed";
    private static final String DEVOPS_JOB_IS_EMPTY = "devops.job.is.empty";

    @Autowired
    private PipelineStageMapper pipelineStageMapper;
    @Autowired
    private CdJobOperator cdJobOperator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineStageDTO pipelineStageDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineStageMapper, pipelineStageDTO, DEVOPS_SAVE_STAGE_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStage(Long pipelineId, Long versionId, PipelineStageVO stage) {
        PipelineStageDTO pipelineStageDTO = ConvertUtils.convertObject(stage, PipelineStageDTO.class);
        pipelineStageDTO.setPipelineId(pipelineId);
        pipelineStageDTO.setVersionId(versionId);
        baseCreate(pipelineStageDTO);

        List<PipelineJobVO> jobList = stage.getJobList();
        if (CollectionUtils.isEmpty(jobList)) {
            throw new CommonException(DEVOPS_JOB_IS_EMPTY);
        }
        jobList.forEach(job -> {
            AbstractCdJobHandler handler = cdJobOperator.getHandler(job.getType());
            if (handler != null) {
//                handler.saveJobInfo(pipelineId, versionId, pipelineStageDTO.getId(), job);
            }
        });
    }
}

