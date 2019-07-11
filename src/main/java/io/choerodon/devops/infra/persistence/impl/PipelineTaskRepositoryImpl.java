package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.PipelineTaskE;
import io.choerodon.devops.domain.application.repository.PipelineTaskRepository;
import io.choerodon.devops.infra.dataobject.PipelineTaskDO;
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
    public PipelineTaskE create(PipelineTaskE pipelineTaskE) {
        PipelineTaskDO appDeployDO = ConvertHelper.convert(pipelineTaskE, PipelineTaskDO.class);
        if (pipelineTaskMapper.insert(appDeployDO) != 1) {
            throw new CommonException("error.insert.pipeline.task");
        }
        return ConvertHelper.convert(appDeployDO, PipelineTaskE.class);
    }

    @Override
    public PipelineTaskE update(PipelineTaskE pipelineTaskE) {
        PipelineTaskDO appDeployDO = ConvertHelper.convert(pipelineTaskE, PipelineTaskDO.class);
        if (pipelineTaskMapper.updateByPrimaryKeySelective(appDeployDO) != 1) {
            throw new CommonException("error.update.pipeline.task");
        }
        return ConvertHelper.convert(appDeployDO, PipelineTaskE.class);
    }

    @Override
    public void deleteById(Long pipelineTaskId) {
        pipelineTaskMapper.deleteByPrimaryKey(pipelineTaskId);
    }

    @Override
    public List<PipelineTaskE> queryByStageId(Long stageId) {
        PipelineTaskDO pipelineTaskDO = new PipelineTaskDO();
        pipelineTaskDO.setStageId(stageId);
        return ConvertHelper.convertList(pipelineTaskMapper.select(pipelineTaskDO), PipelineTaskE.class);
    }

    @Override
    public PipelineTaskE queryById(Long taskId) {
        PipelineTaskDO taskDO = new PipelineTaskDO();
        taskDO.setId(taskId);
        return ConvertHelper.convert(pipelineTaskMapper.selectByPrimaryKey(taskDO), PipelineTaskE.class);
    }

    @Override
    public PipelineTaskE queryByAppDeployId(Long appDeployId) {
        return ConvertHelper.convert(pipelineTaskMapper.queryByAppDeployId(appDeployId), PipelineTaskE.class);
    }
}
