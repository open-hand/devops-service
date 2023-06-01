package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.VulnScanTargetService;
import io.choerodon.devops.infra.dto.VulnScanTargetDTO;
import io.choerodon.devops.infra.mapper.VulnScanTargetMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 漏洞扫描对象记录表(VulnScanTarget)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
@Service
public class VulnScanTargetServiceImpl implements VulnScanTargetService {

    private static final String DEVOPS_SAVE_VULN_TARGET_FAILED = "devops.save.vuln.target.failed";

    @Autowired
    private VulnScanTargetMapper vulnScanTargetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VulnScanTargetDTO baseCreate(Long scanRecordId, String target) {
        VulnScanTargetDTO vulnScanTargetDTO = new VulnScanTargetDTO();
        vulnScanTargetDTO.setScanRecordId(scanRecordId);
        vulnScanTargetDTO.setTarget(target);

        return MapperUtil.resultJudgedInsertSelective(vulnScanTargetMapper, vulnScanTargetDTO, DEVOPS_SAVE_VULN_TARGET_FAILED);
    }

    @Override
    public List<VulnScanTargetDTO> listByRecordId(Long recordId) {
        VulnScanTargetDTO record = new VulnScanTargetDTO();
        record.setScanRecordId(recordId);
        return vulnScanTargetMapper.select(record);
    }
}

