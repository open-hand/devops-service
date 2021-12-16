package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiPipelineSonarDTO;

/**
 * ci任务生成sonar记录(DevopsCiPipelineSonar)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-16 17:56:59
 */
public interface DevopsCiPipelineSonarService {

    void saveSonarInfo(Long jobId, Long gitlabPipelineId, String jobName, String token, String scannerType);

    DevopsCiPipelineSonarDTO queryByPipelineId(Long devopsPipelineId, Long gitlabPipelineId, String jobName);

    void baseCreate(DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO);
}

