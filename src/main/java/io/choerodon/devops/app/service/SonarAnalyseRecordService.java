package io.choerodon.devops.app.service;

import javax.servlet.http.HttpServletRequest;

import io.choerodon.devops.api.vo.sonar.WebhookPayload;

/**
 * 代码扫描记录表(SonarAnalyseRecord)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-06 15:44:31
 */
public interface SonarAnalyseRecordService {

    void forwardWebhook(String payload, HttpServletRequest httpServletRequest);

    void saveAnalyseData(WebhookPayload webhookPayload);
}

