package io.choerodon.devops.app.service.impl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.pipeline.DevopsCiSonarQualityGateVO;
import io.choerodon.devops.api.vo.sonar.*;
import io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.app.task.DevopsCommandRunner;
import io.choerodon.devops.infra.config.SonarConfigProperties;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineSonarDTO;
import io.choerodon.devops.infra.dto.SonarAnalyseRecordDTO;
import io.choerodon.devops.infra.dto.SonarAnalyseUserRecordDTO;
import io.choerodon.devops.infra.enums.SonarQubeType;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.SonarAnalyseRecordMapper;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.RetrofitCallExceptionParse;

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
    private SonarAnalyseUserRecordService sonarAnalyseUserRecordService;

    @Autowired
    private DevopsCiSonarQualityGateService devopsCiSonarQualityGateService;

    @Autowired
    private TransactionalProducer transactionalProducer;

    private static Facet getFacet(String key, String type, SonarClient sonarClient) {
        Map<String, String> map = new HashMap<>();
        map.put("componentKeys", key);
        map.put("facets", "author,severities");
        map.put("types", type);
        return RetrofitCallExceptionParse.executeCallWithTarget(sonarClient.listIssue(map),
                ExceptionConstants.SonarCode.DEVOPS_SONAR_ISSUES_GET,
                new TypeReference<List<Facet>>() {
                },
                "facets").get(0);
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
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ"));
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        WebhookPayload webhookPayload = null;
        try {
            webhookPayload = OBJECT_MAPPER.readValue(payload, WebhookPayload.class);
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
        if (devopsCiSonarQualityGateVO != null) {
            queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,sqale_index,code_smells");
        } else {
            queryContentMap.put("metricKeys", "bugs,vulnerabilities,sqale_index,code_smells");
        }

        //根据project-key查询sonarqube项目内容
        SonarComponent sonarComponent = RetrofitCallExceptionParse.executeCall(sonarClient.listMeasures(queryContentMap),
                ExceptionConstants.SonarCode.DEVOPS_SONAR_MEASURE_GET,
                SonarComponent.class);
        List<Measure> measures = sonarComponent.getComponent().getMeasures();
        webhookPayload.setMeasures(measures);

        Facet bugFacets = getFacet(key, "BUG", sonarClient);
        Facet vulnFacets = getFacet(key, "VULNERABILITY", sonarClient);
        Facet codeSmellFacets = getFacet(key, "CODE_SMELL", sonarClient);
        Map<String, SonarAnalyseUserRecordDTO> userMap = new HashMap<>();

        List<Value> bugValues = bugFacets.getValues();
        if (!CollectionUtils.isEmpty(bugValues)) {
            for (Value bugValue : bugValues) {
                SonarAnalyseUserRecordDTO sonarAnalyseUserRecordDTO = userMap.get(bugValue.getVal());
                if (sonarAnalyseUserRecordDTO == null) {
                    sonarAnalyseUserRecordDTO = new SonarAnalyseUserRecordDTO();
                    sonarAnalyseUserRecordDTO.setUserEmail(bugValue.getVal());
                    sonarAnalyseUserRecordDTO.setBug(bugValue.getCount().longValue());
                    userMap.put(bugValue.getVal(), sonarAnalyseUserRecordDTO);
                } else {
                    sonarAnalyseUserRecordDTO.setBug(bugValue.getCount().longValue());
                }
            }
        }
        List<Value> vulnValues = vulnFacets.getValues();
        if (!CollectionUtils.isEmpty(vulnValues)) {
            for (Value vulnValue : vulnValues) {
                SonarAnalyseUserRecordDTO sonarAnalyseUserRecordDTO = userMap.get(vulnValue.getVal());
                if (sonarAnalyseUserRecordDTO == null) {
                    sonarAnalyseUserRecordDTO = new SonarAnalyseUserRecordDTO();
                    sonarAnalyseUserRecordDTO.setUserEmail(vulnValue.getVal());
                    sonarAnalyseUserRecordDTO.setVulnerability(vulnValue.getCount().longValue());
                    userMap.put(vulnValue.getVal(), sonarAnalyseUserRecordDTO);
                } else {
                    sonarAnalyseUserRecordDTO.setVulnerability(vulnValue.getCount().longValue());
                }
            }
        }

        List<Value> codeSmellValues = codeSmellFacets.getValues();
        if (!CollectionUtils.isEmpty(codeSmellValues)) {
            for (Value codeSmellValue : codeSmellValues) {
                SonarAnalyseUserRecordDTO sonarAnalyseUserRecordDTO = userMap.get(codeSmellValue.getVal());
                if (sonarAnalyseUserRecordDTO == null) {
                    sonarAnalyseUserRecordDTO = new SonarAnalyseUserRecordDTO();
                    sonarAnalyseUserRecordDTO.setUserEmail(codeSmellValue.getVal());
                    sonarAnalyseUserRecordDTO.setCodeSmell(codeSmellValue.getCount().longValue());
                    userMap.put(codeSmellValue.getVal(), sonarAnalyseUserRecordDTO);
                } else {
                    sonarAnalyseUserRecordDTO.setCodeSmell(codeSmellValue.getCount().longValue());
                }
            }
        }
        webhookPayload.setUserMap(userMap);

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
        Map<String, SonarAnalyseUserRecordDTO> userMap = webhookPayload.getUserMap();

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
        for (Measure measure : measures) {
            if (SonarQubeType.BUGS.getType().equals(measure.getMetric())) {
                sonarAnalyseRecordDTO.setBug(Long.parseLong(measure.getValue()));
            }
            if (SonarQubeType.CODE_SMELLS.getType().equals(measure.getMetric())) {
                sonarAnalyseRecordDTO.setCodeSmell(Long.parseLong(measure.getValue()));
            }
            if (SonarQubeType.VULNERABILITIES.getType().equals(measure.getMetric())) {
                sonarAnalyseRecordDTO.setVulnerability(Long.parseLong(measure.getValue()));
            }
            if (SonarQubeType.SQALE_INDEX.getType().equals(measure.getMetric())) {
                sonarAnalyseRecordDTO.setSqaleIndex(Long.parseLong(measure.getValue()));
            }
            if (SonarQubeType.QUALITY_GATE_DETAILS.getType().equals(measure.getMetric())) {
                sonarAnalyseRecordDTO.setQualityGateDetails(measure.getValue());
            }
        }
        MapperUtil.resultJudgedInsertSelective(sonarAnalyseRecordMapper, sonarAnalyseRecordDTO, DEVOPS_SAVE_SONAR_ANALYSE_RECORD_FAILED);

        // 保存用户统计数据
        if (!CollectionUtils.isEmpty(userMap)) {
            sonarAnalyseUserRecordService.batchSave(sonarAnalyseRecordDTO.getId(), userMap.values());
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


    private boolean validSignature(String payload, HttpServletRequest request) {
        String receivedSignature = request.getHeader("X-Sonar-Webhook-HMAC-SHA256");
        // See Apache commons-codec
        String expectedSignature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, sonarConfigProperties.getWebhookToken()).hmacHex(payload);
        return Objects.equals(expectedSignature, receivedSignature);
    }
}

