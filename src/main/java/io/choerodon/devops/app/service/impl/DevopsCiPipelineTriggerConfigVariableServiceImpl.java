package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DevopsCiPipelineTriggerConfigVariableService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigVariableDTO;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineTriggerConfigVariableMapper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCiPipelineTriggerConfigVariableServiceImpl implements DevopsCiPipelineTriggerConfigVariableService {
    private static final String DEVOPS_SAVE_PIPELINE_VARIABLE_FAILED = "devops.save.pipeline.variable.failed";

    @Autowired
    private DevopsCiPipelineTriggerConfigVariableMapper devopsCiPipelineTriggerConfigVariableMapper;


    @Override
    @Transactional
    public void baseCreate(DevopsCiPipelineTriggerConfigVariableDTO devopsCiPipelineVariableDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiPipelineTriggerConfigVariableMapper,
                devopsCiPipelineVariableDTO,
                DEVOPS_SAVE_PIPELINE_VARIABLE_FAILED);
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        DevopsCiPipelineTriggerConfigVariableDTO devopsCiPipelineVariableDTO = new DevopsCiPipelineTriggerConfigVariableDTO();
        devopsCiPipelineVariableDTO.setDevopsPipelineId(pipelineId);
        devopsCiPipelineTriggerConfigVariableMapper.delete(devopsCiPipelineVariableDTO);
    }
}
