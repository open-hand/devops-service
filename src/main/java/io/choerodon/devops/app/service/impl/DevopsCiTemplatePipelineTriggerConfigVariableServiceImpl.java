package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

import io.choerodon.devops.app.service.DevopsCiTemplatePipelineTriggerConfigVariableService;
import io.choerodon.devops.infra.dto.DevopsCiTemplatePipelineTriggerConfigVariableDTO;
import io.choerodon.devops.infra.mapper.DevopsCiTemplatePipelineTriggerConfigVariableMapper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCiTemplatePipelineTriggerConfigVariableServiceImpl implements DevopsCiTemplatePipelineTriggerConfigVariableService {
    private static final String DEVOPS_SAVE_PIPELINE_VARIABLE_FAILED = "devops.save.pipeline.variable.failed";

    @Autowired
    private DevopsCiTemplatePipelineTriggerConfigVariableMapper devopsCiTemplatePipelineTriggerConfigVariableMapper;


    @Override
    @Transactional
    public void baseCreate(DevopsCiTemplatePipelineTriggerConfigVariableDTO devopsCiPipelineVariableDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiTemplatePipelineTriggerConfigVariableMapper,
                devopsCiPipelineVariableDTO,
                DEVOPS_SAVE_PIPELINE_VARIABLE_FAILED);
    }

    @Override
    @Transactional
    public void deleteByPipelineTriggerConfigIds(List<Long> triggerConfigIds) {
        if (ObjectUtils.isEmpty(triggerConfigIds)) {
            return;
        }
        devopsCiTemplatePipelineTriggerConfigVariableMapper.deleteByConfigIds(triggerConfigIds);
    }
}
