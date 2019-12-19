package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/4/19
 */
@NotifyBusinessType(code = NoticeCodeConstants.GITLAB_CONTINUOUS_DELIVERY_FAILURE,
        name = "持续集成流水线失败", level = Level.PROJECT,
        description = "持续集成流水线失败通知", isManualRetry = true, categoryCode = "code-management-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true,
        proPmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        targetUserType = {TargetUserType.TARGET_USER_CODE_SUBMITTER})
@Component
public class GitLabContinuousDeliveryFailurePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "GitLabContinuousDeliveryFailurePm";
    }

    @Override
    public String name() {
        return "GitLab持续集成失败站内信模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.GITLAB_CONTINUOUS_DELIVERY_FAILURE;
    }

    @Override
    public String title() {
        return "持续集成";
    }

    /**
     * projectName appServiceName gitlabUrl organizationCode projectCode appServiceCode gitlabPipelineId
     */
    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下应用服务“${appServiceName}”中的持续集成过程失败</p>\n" +
                "<p><a href=${gitlabUrl}/${organizationCode}-${projectCode}/${appServiceCode}/pipelines/${gitlabPipelineId}>查看详情</a></p>";
    }
}
