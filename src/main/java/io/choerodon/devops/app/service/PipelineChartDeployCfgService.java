package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.cd.PipelineChartDeployCfgVO;
import io.choerodon.devops.infra.dto.PipelineChartDeployCfgDTO;

/**
 * chart部署任务配置表(PipelineChartDeployCfg)应用服务
 *
 * @author
 * @since 2022-11-24 15:57:06
 */
public interface PipelineChartDeployCfgService {


    void baseCreate(PipelineChartDeployCfgDTO pipelineChartDeployCfgDTO);

    void deleteConfigByPipelineId(Long pipelineId);

    PipelineChartDeployCfgDTO queryByConfigId(Long configId);

    PipelineChartDeployCfgVO queryVoByConfigId(Long configId);

    void baseUpdate(PipelineChartDeployCfgDTO pipelineChartDeployCfgDTO);
}

