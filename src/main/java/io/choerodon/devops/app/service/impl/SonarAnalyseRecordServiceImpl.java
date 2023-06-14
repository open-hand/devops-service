package io.choerodon.devops.app.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yqcloud.core.oauth.ZKnowDetailsHelper;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.SonarAnalyseIssueAuthorVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;
import io.choerodon.devops.api.vo.sonar.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.app.task.DevopsCommandRunner;
import io.choerodon.devops.infra.config.SonarConfigProperties;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.enums.SonarQubeType;
import io.choerodon.devops.infra.enums.sonar.IssueFacetEnum;
import io.choerodon.devops.infra.enums.sonar.IssueTypeEnum;
import io.choerodon.devops.infra.enums.sonar.SeverityEnum;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.SonarAnalyseRecordMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.RetrofitCallExceptionParse;
import io.choerodon.devops.infra.util.SonarUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 代码扫描记录表(SonarAnalyseRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
@Service
public class SonarAnalyseRecordServiceImpl implements SonarAnalyseRecordService {

    private static final String DEVOPS_SAVE_SONAR_ANALYSE_RECORD_FAILED = "devops.save.sonar.analyse.record.failed";

    @Autowired
    private SonarAnalyseRecordMapper sonarAnalyseRecordMapper;

    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsCiPipelineSonarService devopsCiPipelineSonarService;
    @Autowired
    private SonarConfigProperties sonarConfigProperties;
    @Autowired
    private SonarAnalyseUserIssueAuthorService sonarAnalyseUserIssueAuthorService;
    @Autowired
    private SonarAnalyseMeasureService sonarAnalyseMeasureService;
    @Autowired
    private SonarAnalyseIssueSeverityService sonarAnalyseIssueSeverityService;
    @Autowired
    private DevopsCiSonarQualityGateService devopsCiSonarQualityGateService;

    @Autowired
    private TransactionalProducer transactionalProducer;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    private static List<Facet> getFacet(String key, String type, SonarClient sonarClient) {
        Map<String, String> map = new HashMap<>();
        map.put("componentKeys", key);
        map.put("facets", "author,severities");
        map.put("types", type);
        return RetrofitCallExceptionParse.executeCallWithTarget(sonarClient.listIssue(map),
                ExceptionConstants.SonarCode.DEVOPS_SONAR_ISSUES_GET,
                new TypeReference<List<Facet>>() {
                },
                "facets");
    }

    private static SonarAnalyseIssueSeverityDTO convertIssueSeverityDTO(IssueTypeEnum type, Facet bugFacet) {
        List<Value> bugValues = bugFacet.getValues();
        if (!CollectionUtils.isEmpty(bugValues)) {
            SonarAnalyseIssueSeverityDTO sonarAnalyseIssueSeverityDTO = new SonarAnalyseIssueSeverityDTO();
            sonarAnalyseIssueSeverityDTO.setType(type.value());
            for (Value bugValue : bugValues) {
                if (SeverityEnum.INFO.value().equals(bugValue.getVal())) {
                    sonarAnalyseIssueSeverityDTO.setInfo(bugValue.getCount().longValue());
                }
                if (SeverityEnum.MINOR.value().equals(bugValue.getVal())) {
                    sonarAnalyseIssueSeverityDTO.setMinor(bugValue.getCount().longValue());
                }
                if (SeverityEnum.MAJOR.value().equals(bugValue.getVal())) {
                    sonarAnalyseIssueSeverityDTO.setMajor(bugValue.getCount().longValue());
                }
                if (SeverityEnum.BLOCKER.value().equals(bugValue.getVal())) {
                    sonarAnalyseIssueSeverityDTO.setBlocker(bugValue.getCount().longValue());
                }
                if (SeverityEnum.CRITICAL.value().equals(bugValue.getVal())) {
                    sonarAnalyseIssueSeverityDTO.setCritical(bugValue.getCount().longValue());
                }
            }
            return sonarAnalyseIssueSeverityDTO;
        }
        return null;
    }

    @Override
    @Saga(productSource = ZKnowDetailsHelper.VALUE_CHOERODON,
            code = SagaTopicCodeConstants.DEVOPS_SAVE_SONAR_ANALYSE_DATA,
            description = "保存代码扫描数据",
            inputSchemaClass = WebhookPayload.class)
    public void forwardWebhook(String payload, HttpServletRequest httpServletRequest) {
        if (Boolean.FALSE.equals(validSignature(payload, httpServletRequest))) {
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ"));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        WebhookPayload webhookPayload = null;
        try {
            webhookPayload = objectMapper.readValue(payload, WebhookPayload.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        Map<String, String> properties = webhookPayload.getProperties();

        String key = webhookPayload.getProject().getKey();

        String c7nAppServiceToken = properties.get("sonar.analysis.c7nAppServiceToken");

        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(c7nAppServiceToken);
        Long appServiceId = appServiceDTO.getId();
        Long projectId = appServiceDTO.getProjectId();

        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarConfigProperties.getUrl(),
                DevopsCommandRunner.SONAR,
                sonarConfigProperties.getUsername(),
                sonarConfigProperties.getPassword());

        DevopsCiSonarQualityGateVO devopsCiSonarQualityGateVO = devopsCiSonarQualityGateService.queryByName(key);


        Map<String, String> queryContentMap = new HashMap<>();
        queryContentMap.put("component", key);
        queryContentMap.put("statuses", "OPEN");
        if (devopsCiSonarQualityGateVO != null) {
            queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,code_smells,sqale_index,sqale_debt_ratio,duplicated_lines");
        } else {
            queryContentMap.put("metricKeys", "bugs,vulnerabilities,code_smells,sqale_index,sqale_debt_ratio,duplicated_lines");
        }

        //根据project-key查询sonarqube项目内容
        SonarComponent sonarComponent = RetrofitCallExceptionParse.executeCall(sonarClient.listMeasures(queryContentMap),
                ExceptionConstants.SonarCode.DEVOPS_SONAR_MEASURE_GET,
                SonarComponent.class);
        List<Measure> measures = sonarComponent.getComponent().getMeasures();
        webhookPayload.setMeasures(measures);

        List<Facet> bugFacets = getFacet(key, IssueTypeEnum.BUG.value(), sonarClient);
        List<Facet> vulnFacets = getFacet(key, IssueTypeEnum.VULNERABILITY.value(), sonarClient);
        List<Facet> codeSmellFacets = getFacet(key, IssueTypeEnum.CODE_SMELL.value(), sonarClient);
        Map<String, SonarAnalyseIssueAuthorDTO> userMap = new HashMap<>();
        List<SonarAnalyseIssueSeverityDTO> sonarAnalyseIssueSeverityDTOList = new ArrayList<>();

        for (Facet bugFacet : bugFacets) {
            if (IssueFacetEnum.AUTHOR.value().equals(bugFacet.getProperty())) {
                List<Value> bugValues = bugFacet.getValues();
                if (!CollectionUtils.isEmpty(bugValues)) {
                    for (Value bugValue : bugValues) {
                        SonarAnalyseIssueAuthorDTO sonarAnalyseIssueAuthorDTO = userMap.get(bugValue.getVal());
                        if (sonarAnalyseIssueAuthorDTO == null) {
                            sonarAnalyseIssueAuthorDTO = new SonarAnalyseIssueAuthorDTO();
                            sonarAnalyseIssueAuthorDTO.setAuthor(bugValue.getVal());
                            sonarAnalyseIssueAuthorDTO.setBug(bugValue.getCount().longValue());
                            sonarAnalyseIssueAuthorDTO.setCodeSmell(0L);
                            sonarAnalyseIssueAuthorDTO.setVulnerability(0L);
                            userMap.put(bugValue.getVal(), sonarAnalyseIssueAuthorDTO);
                        } else {
                            sonarAnalyseIssueAuthorDTO.setBug(bugValue.getCount().longValue());
                        }
                    }
                }
            }
            if (IssueFacetEnum.SEVERITIES.value().equals(bugFacet.getProperty())) {
                sonarAnalyseIssueSeverityDTOList.add(convertIssueSeverityDTO(IssueTypeEnum.BUG, bugFacet));
            }
        }

        for (Facet vulnFacet : vulnFacets) {
            if (IssueFacetEnum.AUTHOR.value().equals(vulnFacet.getProperty())) {
                List<Value> vulnValues = vulnFacet.getValues();
                if (!CollectionUtils.isEmpty(vulnValues)) {
                    for (Value vulnValue : vulnValues) {
                        SonarAnalyseIssueAuthorDTO sonarAnalyseIssueAuthorDTO = userMap.get(vulnValue.getVal());
                        if (sonarAnalyseIssueAuthorDTO == null) {
                            sonarAnalyseIssueAuthorDTO = new SonarAnalyseIssueAuthorDTO();
                            sonarAnalyseIssueAuthorDTO.setAuthor(vulnValue.getVal());
                            sonarAnalyseIssueAuthorDTO.setVulnerability(vulnValue.getCount().longValue());
                            sonarAnalyseIssueAuthorDTO.setCodeSmell(0L);
                            sonarAnalyseIssueAuthorDTO.setBug(0L);
                            userMap.put(vulnValue.getVal(), sonarAnalyseIssueAuthorDTO);
                        } else {
                            sonarAnalyseIssueAuthorDTO.setVulnerability(vulnValue.getCount().longValue());
                        }
                    }
                }
            }
            if (IssueFacetEnum.SEVERITIES.value().equals(vulnFacet.getProperty())) {
                sonarAnalyseIssueSeverityDTOList.add(convertIssueSeverityDTO(IssueTypeEnum.VULNERABILITY, vulnFacet));
            }
        }

        for (Facet codeSmellFacet : codeSmellFacets) {
            if (IssueFacetEnum.AUTHOR.value().equals(codeSmellFacet.getProperty())) {
                List<Value> codeSmellValues = codeSmellFacet.getValues();
                if (!CollectionUtils.isEmpty(codeSmellValues)) {
                    for (Value codeSmellValue : codeSmellValues) {
                        SonarAnalyseIssueAuthorDTO sonarAnalyseIssueAuthorDTO = userMap.get(codeSmellValue.getVal());
                        if (sonarAnalyseIssueAuthorDTO == null) {
                            sonarAnalyseIssueAuthorDTO = new SonarAnalyseIssueAuthorDTO();
                            sonarAnalyseIssueAuthorDTO.setAuthor(codeSmellValue.getVal());
                            sonarAnalyseIssueAuthorDTO.setCodeSmell(codeSmellValue.getCount().longValue());
                            sonarAnalyseIssueAuthorDTO.setBug(0L);
                            sonarAnalyseIssueAuthorDTO.setVulnerability(0L);
                            userMap.put(codeSmellValue.getVal(), sonarAnalyseIssueAuthorDTO);
                        } else {
                            sonarAnalyseIssueAuthorDTO.setCodeSmell(codeSmellValue.getCount().longValue());
                        }
                    }
                }
            }
            if (IssueFacetEnum.SEVERITIES.value().equals(codeSmellFacet.getProperty())) {
                sonarAnalyseIssueSeverityDTOList.add(convertIssueSeverityDTO(IssueTypeEnum.CODE_SMELL, codeSmellFacet));
            }
        }

        webhookPayload.setUserMap(userMap);
        webhookPayload.setSonarAnalyseIssueSeverityList(sonarAnalyseIssueSeverityDTOList);

        transactionalProducer.apply(
                StartSagaBuilder.newBuilder()
                        .withRefType("app")
                        .withRefId(appServiceId.toString())
                        .withSagaCode(SagaTopicCodeConstants.DEVOPS_SAVE_SONAR_ANALYSE_DATA)
                        .withLevel(ResourceLevel.PROJECT)
                        .withSourceId(projectId)
                        .withJson(JsonHelper.marshalByJackson(webhookPayload)),
                builder -> {
                });
    }

    @Override
    public void saveAnalyseData(WebhookPayload webhookPayload) {
        //
        Map<String, String> properties = webhookPayload.getProperties();
        List<Measure> measures = webhookPayload.getMeasures();
        Map<String, SonarAnalyseIssueAuthorDTO> userMap = webhookPayload.getUserMap();

        long gitlabPipelineId = Long.parseLong(properties.get("sonar.analysis.gitlabPipelineId"));
        String gitlabJobName = properties.get("sonar.analysis.gitlabJobName");
        String c7nAppServiceToken = properties.get("sonar.analysis.c7nAppServiceToken");

        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(c7nAppServiceToken);
        Long appServiceId = appServiceDTO.getId();
        Long projectId = appServiceDTO.getProjectId();

        SonarAnalyseRecordDTO sonarAnalyseRecordDTO = new SonarAnalyseRecordDTO();
        sonarAnalyseRecordDTO.setAppServiceId(appServiceId);
        sonarAnalyseRecordDTO.setProjectId(projectId);
        sonarAnalyseRecordDTO.setAnalysedAt(webhookPayload.getAnalysedAt());
        sonarAnalyseRecordDTO.setCommitSha(webhookPayload.getRevision());
        MapperUtil.resultJudgedInsertSelective(sonarAnalyseRecordMapper, sonarAnalyseRecordDTO, DEVOPS_SAVE_SONAR_ANALYSE_RECORD_FAILED);

        List<SonarAnalyseMeasureDTO> sonarAnalyseMeasureDTOS = measures
                .stream()
                .map(m -> new SonarAnalyseMeasureDTO(m.getMetric(), m.getValue()))
                .collect(Collectors.toList());
        sonarAnalyseMeasureService.batchSave(sonarAnalyseRecordDTO.getId(), sonarAnalyseMeasureDTOS);

        List<SonarAnalyseIssueSeverityDTO> sonarAnalyseIssueSeverityList = webhookPayload.getSonarAnalyseIssueSeverityList();
        for (SonarAnalyseIssueSeverityDTO sonarAnalyseIssueSeverityDTO : sonarAnalyseIssueSeverityList) {
            sonarAnalyseIssueSeverityDTO.setRecordId(sonarAnalyseRecordDTO.getId());
            sonarAnalyseIssueSeverityService.baseCreate(sonarAnalyseIssueSeverityDTO);
        }


        // 保存用户统计数据
        if (!CollectionUtils.isEmpty(userMap)) {
            sonarAnalyseUserIssueAuthorService.batchSave(sonarAnalyseRecordDTO.getId(), userMap.values());
        }
        // 保存流水线关联关系
        DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO = devopsCiPipelineSonarService.queryByPipelineId(appServiceId, gitlabPipelineId, gitlabJobName);
        if (devopsCiPipelineSonarDTO != null) {
            devopsCiPipelineSonarDTO.setRecordId(sonarAnalyseRecordDTO.getId());
            devopsCiPipelineSonarService.baseUpdate(devopsCiPipelineSonarDTO);
        }
    }

    @Override
    public SonarAnalyseRecordDTO queryById(Long recordId) {
        return sonarAnalyseRecordMapper.selectByPrimaryKey(recordId);
    }

    @Override
    public Map<Long, Double> listProjectScores(List<Long> actualPids) {
        List<SonarAnalyseRecordDTO> sonarAnalyseRecordDTOS = sonarAnalyseRecordMapper.listProjectLatestRecord(actualPids);
        if (CollectionUtils.isEmpty(sonarAnalyseRecordDTOS)) {
            return new HashMap<>();
        }
        return sonarAnalyseRecordDTOS
                .stream()
                .collect(Collectors.groupingBy(SonarAnalyseRecordDTO::getProjectId,
                        Collectors.averagingDouble(SonarAnalyseRecordDTO::getScore)));
    }

    @Override
    public SonarOverviewVO querySonarOverview(Long projectId) {
        SonarOverviewVO sonarOverviewVO = new SonarOverviewVO();

        List<String> metricTypes = new ArrayList<>();
        metricTypes.add(SonarQubeType.BUGS.getType());
        metricTypes.add(SonarQubeType.VULNERABILITIES.getType());
        metricTypes.add(SonarQubeType.CODE_SMELLS.getType());
        metricTypes.add(SonarQubeType.DUPLICATED_LINES.getType());
        metricTypes.add(SonarQubeType.SQALE_INDEX.getType());
        List<SonarAnalyseMeasureDTO> sonarAnalyseMeasureDTOS = sonarAnalyseMeasureService.listAppLatestMeasures(projectId, metricTypes);
        if (CollectionUtils.isEmpty(sonarAnalyseMeasureDTOS)) {
            return sonarOverviewVO;
        }
        Map<String, Long> measureMap = sonarAnalyseMeasureDTOS
                .stream()
                .collect(Collectors.groupingBy(SonarAnalyseMeasureDTO::getMetric,
                        Collectors.summingLong(i -> Long.parseLong(i.getMetricValue()))));
        measureMap.forEach((k, v) -> {
            if (SonarQubeType.BUGS.getType().equals(k)) {
                sonarOverviewVO.setBugs(v);
            }
            if (SonarQubeType.VULNERABILITIES.getType().equals(k)) {
                sonarOverviewVO.setVulnerabilities(v);
            }
            if (SonarQubeType.CODE_SMELLS.getType().equals(k)) {
                sonarOverviewVO.setCodeSmells(v);
            }
            if (SonarQubeType.DUPLICATED_LINES.getType().equals(k)) {
                sonarOverviewVO.setDuplicatedLines(v);
            }
            if (SonarQubeType.SQALE_INDEX.getType().equals(k)) {
                sonarOverviewVO.setDebt(SonarUtil.caculateSqaleIndex(v));
            }
        });
        return sonarOverviewVO;
    }

    @Override
    public Page<SonarAnalyseIssueAuthorVO> listMemberIssue(Long projectId, Long appServiceId, PageRequest pageRequest) {
        Page<SonarAnalyseIssueAuthorVO> page = PageHelper.doPageAndSort(pageRequest, () -> sonarAnalyseUserIssueAuthorService.listMemberIssue(appServiceId));
        List<SonarAnalyseIssueAuthorVO> content = page.getContent();
        if (CollectionUtils.isEmpty(content)) {
            return page;
        }
        List<String> userEmails = content.stream().map(SonarAnalyseIssueAuthorVO::getAuthor).collect(Collectors.toList());
        List<IamUserDTO> iamUserDTOS = baseServiceClientOperator.listUsersByEmails(userEmails);
        if (!CollectionUtils.isEmpty(iamUserDTOS)) {
            Map<String, IamUserDTO> userMap = iamUserDTOS.stream().collect(Collectors.toMap(IamUserDTO::getEmail, Function.identity()));

            for (SonarAnalyseIssueAuthorVO sonarAnalyseIssueAuthorVO : content) {
                IamUserDTO iamUserDTO = userMap.get(sonarAnalyseIssueAuthorVO.getAuthor());
                if (iamUserDTO != null) {
                    sonarAnalyseIssueAuthorVO.setEmail(iamUserDTO.getEmail());
                    sonarAnalyseIssueAuthorVO.setRealName(iamUserDTO.getRealName());
                    sonarAnalyseIssueAuthorVO.setImageUrl(iamUserDTO.getImageUrl());
                }
            }
        }
        return page;
    }


    private boolean validSignature(String payload, HttpServletRequest request) {
        String receivedSignature = request.getHeader("X-Sonar-Webhook-HMAC-SHA256");
        // See Apache commons-codec
        String expectedSignature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, sonarConfigProperties.getWebhookToken()).hmacHex(payload);
        return Objects.equals(expectedSignature, receivedSignature);
    }
}

