package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.pipeline.DevopsCiPipelineTriggerConfigVO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineTriggerConfigDTO;

public interface DevopsCiPipelineTriggerConfigService {
    DevopsCiPipelineTriggerConfigDTO baseCreate(DevopsCiPipelineTriggerConfigDTO devopsCiPipelineTriggerConfigDTO);

    DevopsCiPipelineTriggerConfigVO queryConfigVoById(Long configId);

    void deleteByJobIds(List<Long> jobIds);
}
