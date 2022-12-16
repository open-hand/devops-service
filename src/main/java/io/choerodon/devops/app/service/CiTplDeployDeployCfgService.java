package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.pipeline.CiDeployDeployCfgVO;

/**
 * CI deployment部署任务配置表(CiTplDeployDeployCfg)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-07 10:15:46
 */
public interface CiTplDeployDeployCfgService {

    CiDeployDeployCfgVO queryConfigVoById(Long configId);
}

