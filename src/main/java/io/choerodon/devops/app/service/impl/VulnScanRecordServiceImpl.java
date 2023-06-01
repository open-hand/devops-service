package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.VulnScanRecordService;
import io.choerodon.devops.infra.dto.VulnScanRecordDTO;
import io.choerodon.devops.infra.mapper.VulnScanRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 漏洞扫描记录表(VulnScanRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
@Service
public class VulnScanRecordServiceImpl implements VulnScanRecordService {

    private static final String DEVOPS_SAVE_VULN_RECORD_FAILED = "devops.save.vuln.record.failed";
    private static final String DEVOPS_UPDATE_VULN_RECORD_FAILED = "devops.update.vuln.record.failed";

    @Autowired
    private VulnScanRecordMapper vulnScanRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VulnScanRecordDTO baseCreate(Long appServiceId, String branchName) {
        VulnScanRecordDTO vulnScanRecordDTO = new VulnScanRecordDTO();
        vulnScanRecordDTO.setAppServiceId(appServiceId);
        vulnScanRecordDTO.setBranchName(branchName);

        return MapperUtil.resultJudgedInsertSelective(vulnScanRecordMapper, vulnScanRecordDTO, DEVOPS_SAVE_VULN_RECORD_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(VulnScanRecordDTO vulnScanRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(vulnScanRecordMapper, vulnScanRecordDTO, DEVOPS_UPDATE_VULN_RECORD_FAILED);
    }
}

