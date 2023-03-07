package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.PipelineJobService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineJobDTO;
import io.choerodon.devops.infra.mapper.PipelineJobMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线任务表(PipelineJob)应用服务
 *
 * @author
 * @since 2022-11-24 15:55:45
 */
@Service
public class PipelineJobServiceImpl implements PipelineJobService {

    private static final String DEVOPS_SAVE_JOB_FAILED = "devops.save.job.failed";

    @Autowired
    private PipelineJobMapper pipelineJobMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineJobDTO pipelineJobDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineJobMapper, pipelineJobDTO, DEVOPS_SAVE_JOB_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineJobDTO pipelineJobDTO = new PipelineJobDTO();
        pipelineJobDTO.setPipelineId(pipelineId);
        pipelineJobMapper.delete(pipelineJobDTO);
    }

    @Override
    public List<PipelineJobDTO> listByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);
        PipelineJobDTO pipelineJobDTO = new PipelineJobDTO();
        pipelineJobDTO.setPipelineId(pipelineId);
        return pipelineJobMapper.select(pipelineJobDTO);
    }

    @Override
    public List<PipelineJobDTO> listByVersionId(Long versionId) {
        Assert.notNull(versionId, PipelineCheckConstant.DEVOPS_PIPELINE_VERSION_ID_IS_NULL);
        PipelineJobDTO pipelineJobDTO = new PipelineJobDTO();
        pipelineJobDTO.setVersionId(versionId);
        return pipelineJobMapper.select(pipelineJobDTO);
    }

    @Override
    public List<PipelineJobDTO> listByStageId(Long stageId) {
        Assert.notNull(stageId, PipelineCheckConstant.DEVOPS_STAGE_ID_IS_NULL);
        PipelineJobDTO pipelineJobDTO = new PipelineJobDTO();
        pipelineJobDTO.setStageId(stageId);
        return pipelineJobMapper.select(pipelineJobDTO);
    }

    @Override
    public PipelineJobDTO baseQueryById(Long id) {
        return pipelineJobMapper.selectByPrimaryKey(id);
    }
}

