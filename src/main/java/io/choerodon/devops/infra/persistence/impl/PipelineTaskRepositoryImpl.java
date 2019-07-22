package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.PipelineTaskRepository;
import io.choerodon.devops.infra.dto.PipelineTaskDTO;
import io.choerodon.devops.infra.mapper.PipelineTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:13 2019/4/4
 * Description:
 */
@Component
public class PipelineTaskRepositoryImpl implements PipelineTaskRepository {
    @Autowired
    private PipelineTaskMapper pipelineTaskMapper;

    @Override
    public PipelineTaskE baseCreateTask(PipelineTaskE pipelineTaskE) {
        PipelineTaskDTO appDeployDO = ConvertHelper.convert(pipelineTaskE, PipelineTaskDTO.class);
        if (pipelineTaskMapper.insert(appDeployDO) != 1) {
            throw new CommonException("error.insert.pipeline.task");
        }
        return ConvertHelper.convert(appDeployDO, PipelineTaskE.class);
    }

    @Override
    public PipelineTaskE baseUpdateTask(PipelineTaskE pipelineTaskE) {
        PipelineTaskDTO appDeployDO = ConvertHelper.convert(pipelineTaskE, PipelineTaskDTO.class);
        if (pipelineTaskMapper.updateByPrimaryKeySelective(appDeployDO) != 1) {
            throw new CommonException("error.update.pipeline.task");
        }
        return ConvertHelper.convert(appDeployDO, PipelineTaskE.class);
    }

    @Override
    public void baseDeleteTaskById(Long pipelineTaskId) {
        pipelineTaskMapper.deleteByPrimaryKey(pipelineTaskId);
    }

    @Override
    public List<PipelineTaskE> baseQueryTaskByStageId(Long stageId) {
        PipelineTaskDTO pipelineTaskDO = new PipelineTaskDTO();
        pipelineTaskDO.setStageId(stageId);
        return ConvertHelper.convertList(pipelineTaskMapper.select(pipelineTaskDO), PipelineTaskE.class);
    }

    @Override
    public PipelineTaskE baseQueryTaskById(Long taskId) {
        PipelineTaskDTO taskDO = new PipelineTaskDTO();
        taskDO.setId(taskId);
        return ConvertHelper.convert(pipelineTaskMapper.selectByPrimaryKey(taskDO), PipelineTaskE.class);
    }

    @Override
    public PipelineTaskE baseQueryTaskByAppDeployId(Long appDeployId) {
        return ConvertHelper.convert(pipelineTaskMapper.queryByAppDeployId(appDeployId), PipelineTaskE.class);
    }
}
