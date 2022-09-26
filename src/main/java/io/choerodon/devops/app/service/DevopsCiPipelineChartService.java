package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.DevopsCiPipelineChartDTO;

/**
 * ci任务生成chart记录(DevopsCiPipelineChart)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-15 17:35:12
 */
public interface DevopsCiPipelineChartService {

    DevopsCiPipelineChartDTO queryByPipelineIdAndJobName(Long appServiceId, Long gitlabPipelineId, String jobName);

    void baseCreate(DevopsCiPipelineChartDTO devopsCiPipelineChartDTO);

    void deleteByAppServiceId(Long appServiceId);
}

