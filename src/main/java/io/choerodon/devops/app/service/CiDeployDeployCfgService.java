package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.CiDeployDeployCfgVO;
import io.choerodon.devops.infra.dto.CiDeployDeployCfgDTO;

/**
 * CI deployment部署任务配置表(CiDeployDeployCfg)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-07 10:15:32
 */
public interface CiDeployDeployCfgService {

    void baseCreate(CiDeployDeployCfgDTO ciDeployDeployCfgDTO);

    CiDeployDeployCfgVO queryConfigVoById(Long configId);

    CiDeployDeployCfgDTO queryConfigById(Long id);

    void updateAppIdAndDeployType(Long id, Long appId, String deployType);

    void deleteConfigByPipelineId(Long ciPipelineId);
}

