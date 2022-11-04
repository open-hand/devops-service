package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.pipeline.CiChartDeployConfigVO;
import io.choerodon.devops.app.service.CiChartDeployConfigService;
import io.choerodon.devops.infra.dto.CiChartDeployConfigDTO;
import io.choerodon.devops.infra.mapper.CiChartDeployConfigMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * CI chart部署任务配置表(CiChartDeployConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-04 14:45:37
 */
@Service
public class CiChartDeployConfigServiceImpl implements CiChartDeployConfigService {

    private static final String DEVOPS_CI_CHART_DEPLOY_CONFIG_SAVE = "devops.ci.chart.deploy.config.save";

    @Autowired
    private CiChartDeployConfigMapper ciChartDeployConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiChartDeployConfigDTO ciChartDeployConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(ciChartDeployConfigMapper, ciChartDeployConfigDTO, DEVOPS_CI_CHART_DEPLOY_CONFIG_SAVE);
    }

    @Override
    public CiChartDeployConfigVO queryConfigVoById(Long id) {
        return ConvertUtils.convertObject(queryConfigVoById(id), CiChartDeployConfigVO.class);
    }

    @Override
    public CiChartDeployConfigDTO queryConfigById(Long id) {
        return ciChartDeployConfigMapper.selectByPrimaryKey(id);
    }

}

