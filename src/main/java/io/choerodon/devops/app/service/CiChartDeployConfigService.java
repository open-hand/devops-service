package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.CiChartDeployConfigVO;
import io.choerodon.devops.infra.dto.CiChartDeployConfigDTO;

/**
 * CI chart部署任务配置表(CiChartDeployConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-04 14:45:36
 */
public interface CiChartDeployConfigService {

    void baseCreate(CiChartDeployConfigDTO ciChartDeployConfigDTO);

    CiChartDeployConfigVO queryConfigVoById(Long id);

    CiChartDeployConfigDTO queryConfigById(Long id);

    void baseUpdate(CiChartDeployConfigDTO ciChartDeployConfigDTO);

    void deleteConfigByPipelineId(Long ciPipelineId);
}

