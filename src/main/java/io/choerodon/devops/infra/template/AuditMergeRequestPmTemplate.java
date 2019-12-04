package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.PmTemplate;

/**
 * @author zmf
 * @since 12/4/19
 */
// TODO by zmf
public class AuditMergeRequestPmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "AuditMergeRequestPm";
    }

    @Override
    public String name() {
        return "审核合并请求站内信模板";
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
        return "<p>项目“${projectName}”下应用服务“${appServiceName}”中${realName}提交了合并请求，需要您进行审核</p>\n" +
                "<p><a href=${gitlabUrl}/${organizationCode}-${projectCode}/${appServiceCode}/merge_requests/${mergeRequestId}>查看详情</a></p>";
    }
}
