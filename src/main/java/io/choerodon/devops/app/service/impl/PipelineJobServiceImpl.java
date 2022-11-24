package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.cd.PipelineJobVO;
import io.choerodon.devops.app.service.PipelineJobService;
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
    public void saveJob(Long pipelineId, Long versionId, Long stageId, PipelineJobVO job) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineJobDTO pipelineJobDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineJobMapper, pipelineJobDTO, DEVOPS_SAVE_JOB_FAILED);
    }
}

