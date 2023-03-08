package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.DevopsCiTemplatePipelineTriggerConfigVO;

public interface DevopsCiTemplatePipelineTriggerConfigService {

    DevopsCiTemplatePipelineTriggerConfigVO queryConfigVoById(Long configId);

    void deleteById(Long configId);
}
