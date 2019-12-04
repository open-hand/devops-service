package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.PmTemplate;

/**
 * @author zmf
 * @since 12/4/19
 */
// TODO by zmf
public class MergeRequestClosedPmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "MergeRequestClosedPm";
    }

    @Override
    public String name() {
        return "关闭MergeRequest站内信模板";
    }

    @Override
    public String businessTypeCode() {
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return "合并请求";
    }

    /**
     * projectName appServiceName realName gitlabUrl organizationCode projectCode appServiceCode mergeRequestId
     */
    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下应用服务“${appServiceName}”中提交的合并请求已被 ${realName} 关闭</p>\n" +
                "<p><a href=${gitlabUrl}/${organizationCode}-${projectCode}/${appServiceCode}/merge_requests/${mergeRequestId}>查看详情</a></p>";
    }
}
