package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiTplVulnScanConfigService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiTplVulnScanConfigDTO;
import io.choerodon.devops.infra.mapper.CiTplVulnScanConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线模板ci 漏洞扫描配置信息表(CiTplVulnScanConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 09:37:23
 */
@Service
public class CiTplVulnScanConfigServiceImpl implements CiTplVulnScanConfigService {

    private static final String DEVOPS_SAVE_TPL_VULN_CONFIG_FAILED = "devops.save.tpl.vuln.config.failed";
    @Autowired
    private CiTplVulnScanConfigMapper ciTplVulnScanConfigMapper;

    @Override
    public CiTplVulnScanConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, PipelineCheckConstant.DEVOPS_PIPELINE_TEMPLATE_ID_IS_NULL);

        CiTplVulnScanConfigDTO record = new CiTplVulnScanConfigDTO();
        record.setCiTemplateStepId(stepId);

        return ciTplVulnScanConfigMapper.selectOne(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiTplVulnScanConfigDTO ciTplVulnScanConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(ciTplVulnScanConfigMapper, ciTplVulnScanConfigDTO, DEVOPS_SAVE_TPL_VULN_CONFIG_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTemplateStepId(Long stepId) {
        Assert.notNull(stepId, PipelineCheckConstant.DEVOPS_PIPELINE_TEMPLATE_ID_IS_NULL);

        CiTplVulnScanConfigDTO record = new CiTplVulnScanConfigDTO();
        record.setCiTemplateStepId(stepId);

        ciTplVulnScanConfigMapper.delete(record);
    }
}

