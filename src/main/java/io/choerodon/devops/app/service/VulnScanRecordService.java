package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.vuln.VulnTargetVO;
import io.choerodon.devops.infra.dto.VulnScanRecordDTO;

/**
 * 漏洞扫描记录表(VulnScanRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:39
 */
public interface VulnScanRecordService {

    VulnScanRecordDTO baseCreate(Long projectId, Long appServiceId, String branchName);

    void baseUpdate(VulnScanRecordDTO vulnScanRecordDTO);

    VulnScanRecordDTO baseQueryById(Long id);

    List<VulnTargetVO> queryDetailsById(Long projectId, Long recordId, String pkgName, String severity, String param);

    Map<Long, Double> listProjectScores(List<Long> actualPids);
}

