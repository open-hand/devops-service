package io.choerodon.devops.app.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.sonar.Measure;
import io.choerodon.devops.api.vo.sonar.SonarComponent;
import io.choerodon.devops.api.vo.sonar.WebhookPayload;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiPipelineSonarService;
import io.choerodon.devops.app.service.SonarAnalyseRecordService;
import io.choerodon.devops.app.task.DevopsCommandRunner;
import io.choerodon.devops.infra.config.SonarConfigProperties;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineSonarDTO;
import io.choerodon.devops.infra.dto.SonarAnalyseRecordDTO;
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

    @Override
    public void saveAnalyseRecord(String payload, HttpServletRequest httpServletRequest) {
        if (Boolean.FALSE.equals(validSignature(payload, httpServletRequest))) {
            return;
        }
        WebhookPayload webhookPayload = JsonHelper.unmarshalByJackson(payload, WebhookPayload.class);
        //
        Map<String, String> properties = webhookPayload.getProperties();

        String key = webhookPayload.getProject().getKey();

        long gitlabPipelineId = Long.parseLong(properties.get("sonar.analysis.gitlabPipelineId"));
        String gitlabJobName = properties.get("sonar.analysis.gitlabJobName");
        String c7nAppServiceToken = properties.get("sonar.analysis.c7nAppServiceToken");

        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(c7nAppServiceToken);
        Long appServiceId = appServiceDTO.getId();
        Long projectId = appServiceDTO.getProjectId();

        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarConfigProperties.getUrl(),
                DevopsCommandRunner.SONAR,
                sonarConfigProperties.getUsername(),
                sonarConfigProperties.getPassword());

        Map<String, String> queryContentMap = new HashMap<>();
        queryContentMap.put("component", key);
        queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,sqale_index,code_smells");

        //根据project-key查询sonarqube项目内容
        SonarComponent sonarComponent = RetrofitCallExceptionParse.executeCall(sonarClient.listMeasures(queryContentMap),
                ExceptionConstants.SonarCode.DEVOPS_SONAR_MEASURE_GET,
                SonarComponent.class);
        List<Measure> measures = sonarComponent.getComponent().getMeasures();

        SonarAnalyseRecordDTO sonarAnalyseRecordDTO = new SonarAnalyseRecordDTO();
        sonarAnalyseRecordDTO.setAppServiceId(appServiceId);
        sonarAnalyseRecordDTO.setProjectId(projectId);
        sonarAnalyseRecordDTO.setAnalysedAt(webhookPayload.getAnalysedAt());
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


        // 保存流水线关联关系
        DevopsCiPipelineSonarDTO devopsCiPipelineSonarDTO = devopsCiPipelineSonarService.queryByPipelineId(appServiceId, gitlabPipelineId, gitlabJobName);
        if (devopsCiPipelineSonarDTO != null) {
            devopsCiPipelineSonarDTO.setRecordId(sonarAnalyseRecordDTO.getId());
            devopsCiPipelineSonarService.baseUpdate(devopsCiPipelineSonarDTO);
        }

    }


    private boolean validSignature(String payload, HttpServletRequest request) {
        String receivedSignature = request.getHeader("X-Sonar-Webhook-HMAC-SHA256");
        // See Apache commons-codec
        String expectedSignature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, sonarConfigProperties.getWebhookToken()).hmacHex(payload);
        return Objects.equals(expectedSignature, receivedSignature);
    }
}

