package io.choerodon.devops.app.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiVulnScanConfigService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiVulnScanConfigDTO;
import io.choerodon.devops.infra.mapper.CiVulnScanConfigMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * ci 漏洞扫描配置信息表(CiVulnScanConfig)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 09:37:11
 */
@Service
public class CiVulnScanConfigServiceImpl implements CiVulnScanConfigService {


    private static final String DEVOPS_SAVE_VULN_CONFIG_FAILED = "devops.save.vuln.config.failed";
    @Autowired
    private CiVulnScanConfigMapper ciVulnScanConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(CiVulnScanConfigDTO vulnScanConfig) {
        MapperUtil.resultJudgedInsertSelective(ciVulnScanConfigMapper, vulnScanConfig, DEVOPS_SAVE_VULN_CONFIG_FAILED);
    }

    @Override
    public CiVulnScanConfigDTO queryByStepId(Long stepId) {
        Assert.notNull(stepId, PipelineCheckConstant.DEVOPS_STEP_ID_IS_NULL);

        CiVulnScanConfigDTO record = new CiVulnScanConfigDTO();
        record.setStepId(stepId);
        return ciVulnScanConfigMapper.selectOne(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByStepIds(Set<Long> stepIds) {
        ciVulnScanConfigMapper.batchDeleteByStepIds(stepIds);
    }
}

