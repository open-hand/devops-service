package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.PipelineTaskService;
import io.choerodon.devops.infra.dto.PipelineTaskDTO;
import io.choerodon.devops.infra.mapper.PipelineTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zmf
 */
@Service
public class PipelineTaskServiceImpl implements PipelineTaskService {
    @Autowired
    private PipelineTaskMapper pipelineTaskMapper;

    @Override
    public PipelineTaskDTO baseCreateTask(PipelineTaskDTO pipelineTaskDTO) {
        if (pipelineTaskMapper.insert(pipelineTaskDTO) != 1) {
            throw new CommonException("error.insert.pipeline.task");
        }
        return pipelineTaskDTO;
    }

    @Override
    public PipelineTaskDTO baseUpdateTask(PipelineTaskDTO pipelineTaskDTO) {
        if (pipelineTaskMapper.updateByPrimaryKeySelective(pipelineTaskDTO) != 1) {
            throw new CommonException("error.update.pipeline.task");
        }
        return pipelineTaskDTO;
    }

    @Override
    public void baseDeleteTaskById(Long pipelineTaskId) {
        pipelineTaskMapper.deleteByPrimaryKey(pipelineTaskId);
    }

    @Override
    public List<PipelineTaskDTO> baseQueryTaskByStageId(Long stageId) {
        PipelineTaskDTO pipelineTaskDTO = new PipelineTaskDTO();
        pipelineTaskDTO.setStageId(stageId);
        return pipelineTaskMapper.select(pipelineTaskDTO);
    }

    @Override
    public PipelineTaskDTO baseQueryTaskById(Long taskId) {
        PipelineTaskDTO taskDO = new PipelineTaskDTO();
        taskDO.setId(taskId);
        return pipelineTaskMapper.selectByPrimaryKey(taskDO);
    }

    @Override
    public PipelineTaskDTO baseQueryTaskByAppDeployId(Long appDeployId) {
        return pipelineTaskMapper.queryByAppDeployId(appDeployId);
    }
}
