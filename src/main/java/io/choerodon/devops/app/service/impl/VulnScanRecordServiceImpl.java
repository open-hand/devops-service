package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.vuln.VulnTargetVO;
import io.choerodon.devops.app.service.VulnScanRecordService;
import io.choerodon.devops.app.service.VulnScanTargetService;
import io.choerodon.devops.app.service.VulnTargetRelService;
import io.choerodon.devops.infra.dto.VulnScanRecordDTO;
import io.choerodon.devops.infra.dto.VulnScanTargetDTO;
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
    @Autowired
    private VulnScanTargetService vulnScanTargetService;
    @Autowired
    private VulnTargetRelService vulnTargetRelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VulnScanRecordDTO baseCreate(Long appServiceId, String branchName) {
        VulnScanRecordDTO vulnScanRecordDTO = new VulnScanRecordDTO();
        vulnScanRecordDTO.setAppServiceId(appServiceId);
        vulnScanRecordDTO.setBranchName(branchName);
        VulnScanRecordDTO vulnScanRecordDTO1 = MapperUtil.resultJudgedInsertSelective(vulnScanRecordMapper, vulnScanRecordDTO, DEVOPS_SAVE_VULN_RECORD_FAILED);
        return vulnScanRecordMapper.selectByPrimaryKey(vulnScanRecordDTO1.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(VulnScanRecordDTO vulnScanRecordDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(vulnScanRecordMapper, vulnScanRecordDTO, DEVOPS_UPDATE_VULN_RECORD_FAILED);
    }

    @Override
    public VulnScanRecordDTO baseQueryById(Long id) {
        return vulnScanRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<VulnTargetVO> queryDetailsById(Long projectId, Long recordId, String pkgName, String severity, String param) {
        List<VulnTargetVO> vulnTargetVOS = new ArrayList<>();
        List<VulnScanTargetDTO> vulnScanTargetDTOS = vulnScanTargetService.listByRecordId(recordId);
        if (CollectionUtils.isEmpty(vulnScanTargetDTOS)) {
            return vulnTargetVOS;
        }
        for (VulnScanTargetDTO vulnScanTargetDTO : vulnScanTargetDTOS) {
            vulnTargetVOS.add(new VulnTargetVO(vulnScanTargetDTO.getTarget(), vulnTargetRelService.listByTargetId(vulnScanTargetDTO.getId(), pkgName, severity, param)));
        }

        return vulnTargetVOS;
    }
}

