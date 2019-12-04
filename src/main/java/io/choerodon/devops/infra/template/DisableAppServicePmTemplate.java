package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.PmTemplate;

/**
 * @author zmf
 * @since 12/4/19
 */
// TODO by zmf
public class DisableAppServicePmTemplate implements PmTemplate {
    @Override
    public String code() {
        // TODO by zmf
        return null;
    }

    @Override
    public String name() {
        // TODO by zmf
        return null;
    }

    @Override
    public String businessTypeCode() {
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return "应用服务已停用";
    }

    /**
     * projectName appServiceName projectId projectName projectCategory organizationId
     */
    @Override
    public String content() {
        return "<p>项目“${projectName}”下的应用服务“${appServiceName}”已被停用</p>\n" +
                "<p><a href=#/devops/app-service?type=project&id=${projectId}&name=${projectName}&category=${projectCategory}&organizationId=${organizationId}>查看详情</a></p>";
    }
}
