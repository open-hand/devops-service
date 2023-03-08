package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

import io.choerodon.devops.api.vo.pipeline.DevopsCiTemplatePipelineTriggerConfigVO;
import io.choerodon.devops.app.service.DevopsCiTemplatePipelineTriggerConfigService;
import io.choerodon.devops.app.service.DevopsCiTemplatePipelineTriggerConfigVariableService;
import io.choerodon.devops.infra.mapper.DevopsCiTemplatePipelineTriggerConfigMapper;

@Service
public class DevopsCiTemplatePipelineTriggerConfigServiceImpl implements DevopsCiTemplatePipelineTriggerConfigService {
    @Autowired
    private DevopsCiTemplatePipelineTriggerConfigMapper devopsCiTemplatePipelineTriggerConfigMapper;

    @Autowired
    private DevopsCiTemplatePipelineTriggerConfigVariableService devopsCiTemplatePipelineTriggerConfigVariableService;


    @Override
    public DevopsCiTemplatePipelineTriggerConfigVO queryConfigVoById(Long configId) {
        return devopsCiTemplatePipelineTriggerConfigMapper.queryConfigVoById(configId);
    }

    @Override
    public void deleteById(Long configId) {
        devopsCiTemplatePipelineTriggerConfigMapper.deleteByPrimaryKey(configId);
        devopsCiTemplatePipelineTriggerConfigVariableService.deleteByPipelineTriggerConfigIds(Collections.singletonList(configId));
    }
}
