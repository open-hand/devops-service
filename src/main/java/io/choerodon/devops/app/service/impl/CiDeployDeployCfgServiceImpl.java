package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.pipeline.CiDeployDeployCfgVO;
import io.choerodon.devops.app.service.CiDeployDeployCfgService;
import io.choerodon.devops.infra.dto.CiDeployDeployCfgDTO;
import io.choerodon.devops.infra.mapper.CiDeployDeployCfgMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * CI deployment部署任务配置表(CiDeployDeployCfg)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-07 10:15:32
 */
@Service
public class CiDeployDeployCfgServiceImpl implements CiDeployDeployCfgService {

    private static final String DEVOPS_DEPLOYMENT_CONFIG_SAVE = "devops.deployment.config.save";


    @Autowired
    private CiDeployDeployCfgMapper ciDeployDeployCfgMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiDeployDeployCfgDTO ciDeployDeployCfgDTO) {
        MapperUtil.resultJudgedInsertSelective(ciDeployDeployCfgMapper, ciDeployDeployCfgDTO, DEVOPS_DEPLOYMENT_CONFIG_SAVE);
    }

    @Override
    public CiDeployDeployCfgVO queryConfigVoById(Long configId) {
        return ConvertUtils.convertObject(ciDeployDeployCfgMapper.selectByPrimaryKey(configId), CiDeployDeployCfgVO.class);
    }
}

