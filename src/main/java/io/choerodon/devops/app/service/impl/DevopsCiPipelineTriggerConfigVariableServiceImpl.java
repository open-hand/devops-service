package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import io.choerodon.devops.app.service.DevopsCiPipelineTriggerConfigVariableService;
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
    public void deleteByPipelineTriggerConfigIds(List<Long> triggerConfigIds) {
        if (ObjectUtils.isEmpty(triggerConfigIds)) {
            return;
        }
        devopsCiPipelineTriggerConfigVariableMapper.deleteByConfigIds(triggerConfigIds);
    }
}
