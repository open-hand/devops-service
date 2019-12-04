package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.PmTemplate;

/**
 * @author zmf
 * @since 12/4/19
 */
// TODO by zmf
public class EnableAppServicePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "EnableAppServicePm";
    }

    @Override
    public String name() {
        return "启用应用服务站内信模板";
    }

    @Override
    public String businessTypeCode() {
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return "应用服务已启用";
    }

    /**
     * projectName appServiceName projectId projectName projectCategory organizationId
     */
    @Override
    public String content() {
        return "<p>项目“${projectName}”下的应用服务“${appServiceName}”已被启用</p>\n" +
                "<p><a href=#/devops/app-service?type=project&id=${projectId}&name=${projectName}&category=${projectCategory}&organizationId=${organizationId}>查看详情</a></p>";
    }
}
