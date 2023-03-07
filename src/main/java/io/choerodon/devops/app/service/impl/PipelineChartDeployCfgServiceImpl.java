package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.api.vo.cd.PipelineChartDeployCfgVO;
import io.choerodon.devops.app.service.PipelineChartDeployCfgService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineChartDeployCfgDTO;
import io.choerodon.devops.infra.mapper.PipelineChartDeployCfgMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * chart部署任务配置表(PipelineChartDeployCfg)应用服务
 *
 * @author
 * @since 2022-11-24 15:57:06
 */
@Service
public class PipelineChartDeployCfgServiceImpl implements PipelineChartDeployCfgService {

    private static final String DEVOPS_SAVE_CHART_DEPLOY_CONFIG_FAILED = "devops.save.chart.deploy.config.failed";
    private static final String DEVOPS_UPDATE_CHART_DEPLOY_CONFIG_FAILED = "devops.update.chart.deploy.config.failed";

    @Autowired
    private PipelineChartDeployCfgMapper pipelineChartDeployCfgMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(PipelineChartDeployCfgDTO pipelineChartDeployCfgDTO) {
        MapperUtil.resultJudgedInsertSelective(pipelineChartDeployCfgMapper, pipelineChartDeployCfgDTO, DEVOPS_SAVE_CHART_DEPLOY_CONFIG_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineChartDeployCfgDTO pipelineChartDeployCfgDTO = new PipelineChartDeployCfgDTO();
        pipelineChartDeployCfgDTO.setPipelineId(pipelineId);
        pipelineChartDeployCfgMapper.delete(pipelineChartDeployCfgDTO);

    }

    @Override
    public PipelineChartDeployCfgVO queryVoByConfigId(Long configId) {
        return pipelineChartDeployCfgMapper.queryVoByConfigId(configId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(PipelineChartDeployCfgDTO pipelineChartDeployCfgDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelineChartDeployCfgMapper, pipelineChartDeployCfgDTO, DEVOPS_UPDATE_CHART_DEPLOY_CONFIG_FAILED);

    }

    @Override
    public PipelineChartDeployCfgDTO queryByConfigId(Long configId) {
        return pipelineChartDeployCfgMapper.selectByPrimaryKey(configId);
    }
}

