package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.api.vo.pipeline.CiChartDeployConfigVO;
import io.choerodon.devops.app.service.CiChartDeployConfigService;
import io.choerodon.devops.app.service.DevopsDeployValueService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiChartDeployConfigDTO;
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;
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
    private static final String DEVOPS_CI_CHART_DEPLOY_CONFIG_UPDATE = "devops.ci.chart.deploy.config.update";

    @Autowired
    private CiChartDeployConfigMapper ciChartDeployConfigMapper;
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiChartDeployConfigDTO ciChartDeployConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(ciChartDeployConfigMapper, ciChartDeployConfigDTO, DEVOPS_CI_CHART_DEPLOY_CONFIG_SAVE);
    }

    @Override
    public CiChartDeployConfigVO queryConfigVoById(Long id) {
        CiChartDeployConfigVO ciChartDeployConfigVO = ConvertUtils.convertObject(queryConfigById(id), CiChartDeployConfigVO.class);
        DevopsDeployValueDTO devopsDeployValueDTO = devopsDeployValueService.baseQueryById(ciChartDeployConfigVO.getValueId());
        if (devopsDeployValueDTO != null) {
            ciChartDeployConfigVO.setValue(devopsDeployValueDTO.getValue());
        }
        return ciChartDeployConfigVO;
    }

    @Override
    public CiChartDeployConfigDTO queryConfigById(Long id) {
        return ciChartDeployConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(CiChartDeployConfigDTO ciChartDeployConfigDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(ciChartDeployConfigMapper, ciChartDeployConfigDTO, DEVOPS_CI_CHART_DEPLOY_CONFIG_UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigByPipelineId(Long ciPipelineId) {
        Assert.notNull(ciPipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        CiChartDeployConfigDTO ciChartDeployConfigDTO = new CiChartDeployConfigDTO();
        ciChartDeployConfigDTO.setCiPipelineId(ciPipelineId);
        ciChartDeployConfigMapper.delete(ciChartDeployConfigDTO);
    }

}

