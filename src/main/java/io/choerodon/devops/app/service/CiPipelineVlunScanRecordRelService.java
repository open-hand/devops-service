package io.choerodon.devops.app.service;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.infra.dto.VulnScanRecordDTO;

/**
 * ci流水线漏洞扫描记录关系表(CiPipelineVlunScanRecordRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:24
 */
public interface CiPipelineVlunScanRecordRelService {

    void uploadVulnResult(Long gitlabPipelineId, String jobName, String branchName, String token, Long configId, MultipartFile file);

    VulnScanRecordDTO queryScanRecordInfo(Long appServiceId, Long gitlabPipelineId, String name);
}

