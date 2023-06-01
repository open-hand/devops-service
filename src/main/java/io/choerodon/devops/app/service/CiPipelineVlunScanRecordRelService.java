package io.choerodon.devops.app.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * ci流水线漏洞扫描记录关系表(CiPipelineVlunScanRecordRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:24
 */
public interface CiPipelineVlunScanRecordRelService {

    void uploadVulnResult(Long gitlabPipelineId, String jobName, String branchName, String token, MultipartFile file);
}

