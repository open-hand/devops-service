package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.CiChartDeployConfigVO;
import io.choerodon.devops.infra.dto.CiTplChartDeployCfgDTO;

/**
 * 流水线模板 chart部署任务配置表(CiTplChartDeployCfg)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-04 15:04:59
 */
public interface CiTplChartDeployCfgService {

    CiChartDeployConfigVO queryConfigVoById(Long id);

    CiTplChartDeployCfgDTO queryConfigById(Long id);
}

