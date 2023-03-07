package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.api.vo.cd.PipelineStageVO;
import io.choerodon.devops.app.eventhandler.cd.AbstractCdJobHandler;
import io.choerodon.devops.app.eventhandler.cd.CdJobOperator;
import io.choerodon.devops.app.service.PipelineStageService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
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
    public void saveStage(Long projectId, Long pipelineId, Long versionId, PipelineStageVO stage) {
        PipelineStageDTO pipelineStageDTO = ConvertUtils.convertObject(stage, PipelineStageDTO.class);
        pipelineStageDTO.setId(null);
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
                handler.saveJobInfo(projectId, pipelineId, versionId, pipelineStageDTO.getId(), job);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setPipelineId(pipelineId);
        pipelineStageMapper.delete(pipelineStageDTO);
    }

    @Override
    public List<PipelineStageDTO> listByVersionId(Long versionId) {
        Assert.notNull(versionId, PipelineCheckConstant.DEVOPS_PIPELINE_VERSION_ID_IS_NULL);

        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setVersionId(versionId);
        return pipelineStageMapper.select(pipelineStageDTO);
    }
}

