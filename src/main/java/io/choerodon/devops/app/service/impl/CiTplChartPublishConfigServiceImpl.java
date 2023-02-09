package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.infra.dto.CiChartPublishConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiTplChartPublishConfigService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiTplChartPublishConfigDTO;
import io.choerodon.devops.infra.mapper.CiTplChartPublishConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线模板chart发布配置(CiTplChartPublishConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-01-09 14:37:57
 */
@Service
public class CiTplChartPublishConfigServiceImpl implements CiTplChartPublishConfigService {

    private static final String DEVOPS_SAVE_TPL_CHART_PUBLISH_CFG_FAILED = "devops.save.tpl.chart.publish.cfg.failed";
    @Autowired
    private CiTplChartPublishConfigMapper ciTplChartPublishConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiTplChartPublishConfigDTO ciTplChartPublishConfigDTO) {
        ciTplChartPublishConfigDTO.setId(null);
        MapperUtil.resultJudgedInsertSelective(ciTplChartPublishConfigMapper,
                ciTplChartPublishConfigDTO,
                DEVOPS_SAVE_TPL_CHART_PUBLISH_CFG_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateStepId(Long templateId) {
        Assert.notNull(templateId, PipelineCheckConstant.DEVOPS_PIPELINE_TEMPLATE_ID_IS_NULL);

        CiTplChartPublishConfigDTO ciTplChartPublishConfigDTO = new CiTplChartPublishConfigDTO();
        ciTplChartPublishConfigDTO.setCiTemplateStepId(templateId);

        ciTplChartPublishConfigMapper.delete(ciTplChartPublishConfigDTO);
    }

    @Override
    public CiTplChartPublishConfigDTO queryByStepId(Long stepTemplateId) {
        Assert.notNull(stepTemplateId, PipelineCheckConstant.DEVOPS_STEP_ID_IS_NULL);
        CiTplChartPublishConfigDTO ciTplChartPublishConfigDTO = new CiTplChartPublishConfigDTO();
        ciTplChartPublishConfigDTO.setCiTemplateStepId(stepTemplateId);
        return ciTplChartPublishConfigMapper.selectOne(ciTplChartPublishConfigDTO);
    }
}

