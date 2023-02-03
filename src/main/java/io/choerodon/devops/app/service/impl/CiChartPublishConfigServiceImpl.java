package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.CiChartPublishConfigService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiChartPublishConfigDTO;
import io.choerodon.devops.infra.mapper.CiChartPublishConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线chart发布配置(CiChartPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-04 15:28:30
 */
@Service
public class CiChartPublishConfigServiceImpl implements CiChartPublishConfigService {

    private static final String DEVOPS_SAVE_CHART_PUBLISH_CONFIG_FAILED = "devops.save.chart.publish.config.failed";

    @Autowired
    private CiChartPublishConfigMapper ciChartPublishConfigMapper;

    @Override
    public CiChartPublishConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, PipelineCheckConstant.DEVOPS_STEP_ID_IS_NULL);
        CiChartPublishConfigDTO ciChartPublishConfigDTO = new CiChartPublishConfigDTO();
        ciChartPublishConfigDTO.setStepId(stepId);
        return ciChartPublishConfigMapper.selectOne(ciChartPublishConfigDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiChartPublishConfigDTO chartPublishConfig) {
        MapperUtil.resultJudgedInsertSelective(ciChartPublishConfigMapper, chartPublishConfig, DEVOPS_SAVE_CHART_PUBLISH_CONFIG_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByStepIds(Set<Long> stepIds) {
        if (!CollectionUtils.isEmpty(stepIds)) {
            ciChartPublishConfigMapper.batchDeleteByStepIds(stepIds);
        }
    }
}

