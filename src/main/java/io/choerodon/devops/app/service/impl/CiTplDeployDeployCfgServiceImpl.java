package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.pipeline.CiDeployDeployCfgVO;
import io.choerodon.devops.app.service.CiTplDeployDeployCfgService;
import io.choerodon.devops.infra.mapper.CiTplDeployDeployCfgMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * CI deployment部署任务配置表(CiTplDeployDeployCfg)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-07 10:15:46
 */
@Service
public class CiTplDeployDeployCfgServiceImpl implements CiTplDeployDeployCfgService {
    @Autowired
    private CiTplDeployDeployCfgMapper ciTplDeployDeployCfgMapper;

    @Override
    public CiDeployDeployCfgVO queryConfigVoById(Long configId) {
        return ConvertUtils.convertObject(ciTplDeployDeployCfgMapper.selectByPrimaryKey(configId), CiDeployDeployCfgVO.class);
    }
}

