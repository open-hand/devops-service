package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.vuln.VulnTargetDTO;
import io.choerodon.devops.infra.dto.vuln.VulnerabilityDTO;
import io.choerodon.devops.infra.enums.ImageSecurityEnum;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.mapper.CiPipelineVlunScanRecordRelMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
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
    @Autowired
    private DevopsCiDockerBuildConfigService devopsCiDockerBuildConfigService;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        // 不区分属性的大小写 比如Target 转换为target
        OBJECT_MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    private static void securityMonitor(Long integer, DevopsCiDockerBuildConfigDTO securityConditionConfigVO) {
        if (StringUtils.equalsIgnoreCase("<=", securityConditionConfigVO.getSecurityControlConditions())) {
            if (integer > securityConditionConfigVO.getVulnerabilityCount()) {
                throw new DevopsCiInvalidException("Does not meet the security control conditions," + securityConditionConfigVO.getSeverity()
                        + " loophole count:" + integer);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadVulnResult(Long gitlabPipelineId, String jobName, String branchName, String token, Long configId, MultipartFile file) {
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
            JsonNode jsonNode = OBJECT_MAPPER.readTree(file.getBytes());
            List<VulnTargetDTO> results = OBJECT_MAPPER.readValue(jsonNode.get("Results").toString(), new TypeReference<List<VulnTargetDTO>>() {
            });
            long unknownCount = 0;
            long lowCount = 0;
            long mediumCount = 0;
            long highCount = 0;
            long criticalCount = 0;
            if (!CollectionUtils.isEmpty(results)) {
                Set<io.choerodon.devops.infra.dto.VulnerabilityDTO> vulnerabilityDTOList = new HashSet<>();
                for (VulnTargetDTO result : results) {
                    // 保存扫描对象
                    VulnScanTargetDTO vulnScanTargetDTO = vulnScanTargetService.baseCreate(vulnScanRecordDTO.getId(), result.getTarget());

                    List<VulnerabilityDTO> vulnerabilities = result.getVulnerabilities();
                    List<VulnTargetRelDTO> vulnTargetRelDTOList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(vulnerabilities)) {
                        for (VulnerabilityDTO vulnerability : vulnerabilities) {
                            vulnerabilityDTOList.add(ConvertUtils.convertObject(vulnerability, io.choerodon.devops.infra.dto.VulnerabilityDTO.class));
                            vulnTargetRelDTOList.add(new VulnTargetRelDTO(vulnScanTargetDTO.getId(),
                                    vulnerability.getPkgName(),
                                    vulnerability.getInstalledVersion(),
                                    vulnerability.getFixedVersion(),
                                    vulnerability.getVulnerabilityId()));
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
            // 质量门禁
            if (configId != null) {
                DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = devopsCiDockerBuildConfigService.baseQuery(configId);
                if (Boolean.TRUE.equals(devopsCiDockerBuildConfigDTO.getSecurityControl())) {
                    switch (ImageSecurityEnum.valueOf(devopsCiDockerBuildConfigDTO.getSeverity())) {
                        case HIGH:
                            securityMonitor(vulnScanRecordDTO.getHigh(), devopsCiDockerBuildConfigDTO);
                            break;
                        case CRITICAL:
                            securityMonitor(vulnScanRecordDTO.getCritical(), devopsCiDockerBuildConfigDTO);
                            break;
                        case MEDIUM:
                            securityMonitor(vulnScanRecordDTO.getMedium(), devopsCiDockerBuildConfigDTO);
                            break;
                        case LOW:
                            securityMonitor(vulnScanRecordDTO.getLow(), devopsCiDockerBuildConfigDTO);
                            break;
                        default:
                            throw new DevopsCiInvalidException("security level not exist: {}", devopsCiDockerBuildConfigDTO.getSeverity());
                    }
                }
            }
        } catch (IOException e) {
            throw new DevopsCiInvalidException(e);
        }
    }

    @Override
    public VulnScanRecordDTO queryScanRecordInfo(Long appServiceId, Long gitlabPipelineId, String jobName) {
        CiPipelineVlunScanRecordRelDTO ciPipelineVlunScanRecordRelDTO = new CiPipelineVlunScanRecordRelDTO(appServiceId,
                gitlabPipelineId,
                jobName);
        CiPipelineVlunScanRecordRelDTO ciPipelineVlunScanRecordRelDTO1 = ciPipelineVlunScanRecordRelMapper.selectOne(ciPipelineVlunScanRecordRelDTO);
        if (ciPipelineVlunScanRecordRelDTO1 == null) {
            return null;
        }

        return vulnScanRecordService.baseQueryById(ciPipelineVlunScanRecordRelDTO1.getScanRecordId());
    }
}

