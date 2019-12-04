package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.PmTemplate;

/**
 * @author zmf
 * @since 12/4/19
 */
// TODO by zmf
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
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return "持续集成";
    }

    /**
     * projectName appServiceName gitlabUrl organizationCode projectCode appServiceCode
     */
    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下应用服务“${appServiceName}”中的持续集成过程失败</p>\n" +
                "<p><a href=${gitlabUrl}/${organizationCode}-${projectCode}/${appServiceCode}/pipelines>查看详情</a></p>";
    }
}
