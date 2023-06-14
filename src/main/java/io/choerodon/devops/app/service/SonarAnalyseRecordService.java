package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import io.choerodon.devops.api.vo.sonar.SonarOverviewVO;
import io.choerodon.devops.api.vo.sonar.WebhookPayload;
import io.choerodon.devops.infra.dto.SonarAnalyseRecordDTO;

/**
 * 代码扫描记录表(SonarAnalyseRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
public interface SonarAnalyseRecordService {

    void forwardWebhook(String payload, HttpServletRequest httpServletRequest);

    void saveAnalyseData(WebhookPayload webhookPayload);

    SonarAnalyseRecordDTO queryById(Long recordId);

    Map<Long, Double> listProjectScores(List<Long> actualPids);

    SonarOverviewVO querySonarOverview(Long projectId);
}

