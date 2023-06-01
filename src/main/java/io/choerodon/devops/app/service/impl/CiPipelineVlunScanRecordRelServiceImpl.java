package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.api.vo.vuln.VulnTargetVO;
import io.choerodon.devops.api.vo.vuln.VulnerabilityVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.ImageSecurityEnum;
import io.choerodon.devops.infra.mapper.CiPipelineVlunScanRecordRelMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * ci流水线漏洞扫描记录关系表(CiPipelineVlunScanRecordRel)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-05-31 15:27:24
 */
@Service
public class CiPipelineVlunScanRecordRelServiceImpl implements CiPipelineVlunScanRecordRelService {

    private static final String DEVOPS_SAVE_PIPELINE_SCAN_RECORD_FAILED = "devops.save.pipeline.scan.record.failed";

    @Autowired
    private CiPipelineVlunScanRecordRelMapper ciPipelineVlunScanRecordRelMapper;
    @Autowired
    private VulnScanRecordService vulnScanRecordService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private VulnScanTargetService vulnScanTargetService;
    @Autowired
    private VulnTargetRelService vulnTargetRelService;
    @Autowired
    private VulnerabilityService vulnerabilityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadVulnResult(Long gitlabPipelineId, String jobName, String branchName, String token, MultipartFile file) {
        AppServiceDTO appServiceDTO = appServiceService.queryByTokenOrThrowE(token);
        Long appServiceId = appServiceDTO.getId();
        try {
            // 初始化扫描记录
            VulnScanRecordDTO vulnScanRecordDTO = vulnScanRecordService.baseCreate(appServiceId, branchName);

            // 保存流水线漏洞扫描记录
            MapperUtil.resultJudgedInsertSelective(ciPipelineVlunScanRecordRelMapper, new CiPipelineVlunScanRecordRelDTO(appServiceId,
                            gitlabPipelineId,
                            jobName,
                            vulnScanRecordDTO.getId()),
                    DEVOPS_SAVE_PIPELINE_SCAN_RECORD_FAILED);
            JsonNode jsonNode = JsonHelper.OBJECT_MAPPER.readTree(file.getBytes());
            List<VulnTargetVO> results = JsonHelper.unmarshalByJackson(jsonNode.get("Results").toString(), new TypeReference<List<VulnTargetVO>>() {
            });
            long unknownCount = 0;
            long lowCount = 0;
            long mediumCount = 0;
            long highCount = 0;
            long criticalCount = 0;
            if (!CollectionUtils.isEmpty(results)) {
                Set<VulnerabilityDTO> vulnerabilityDTOList = new HashSet<>();
                for (VulnTargetVO result : results) {
                    // 保存扫描对象
                    VulnScanTargetDTO vulnScanTargetDTO = vulnScanTargetService.baseCreate(vulnScanRecordDTO.getId(), result.getTarget());

                    List<VulnerabilityVO> vulnerabilities = result.getVulnerabilities();
                    List<VulnTargetRelDTO> vulnTargetRelDTOList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(vulnerabilities)) {
                        for (VulnerabilityVO vulnerability : vulnerabilities) {
                            vulnerabilityDTOList.add(ConvertUtils.convertObject(vulnerability, VulnerabilityDTO.class));
                            vulnTargetRelDTOList.add(new VulnTargetRelDTO(vulnScanTargetDTO.getId(), vulnerability.getVulnerabilityID()));
                            if (ImageSecurityEnum.UNKNOWN.getValue().equals(vulnerability.getSeverity())) {
                                unknownCount++;
                            }
                            if (ImageSecurityEnum.LOW.getValue().equals(vulnerability.getSeverity())) {
                                lowCount++;
                            }
                            if (ImageSecurityEnum.MEDIUM.getValue().equals(vulnerability.getSeverity())) {
                                mediumCount++;
                            }
                            if (ImageSecurityEnum.HIGH.getValue().equals(vulnerability.getSeverity())) {
                                highCount++;
                            }
                            if (ImageSecurityEnum.CRITICAL.getValue().equals(vulnerability.getSeverity())) {
                                criticalCount++;
                            }
                        }
                    }
                    // 保存扫描对象漏洞关联关系
                    vulnTargetRelService.batchSave(vulnTargetRelDTOList);
                }
                // 更新漏洞
                vulnerabilityService.btachSave(vulnerabilityDTOList);
            }

            // 更新扫描记录
            vulnScanRecordDTO.setUnknown(unknownCount);
            vulnScanRecordDTO.setLow(lowCount);
            vulnScanRecordDTO.setMedium(mediumCount);
            vulnScanRecordDTO.setHigh(highCount);
            vulnScanRecordDTO.setCritical(criticalCount);
            vulnScanRecordService.baseUpdate(vulnScanRecordDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

