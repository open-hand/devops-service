package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.PmTemplate;

/**
 * 应用服务创建失败的站内信模板
 *
 * @author zmf
 * @since 12/4/19
 */
// TODO @Component
public class AppServiceCreationFailurePmTemplate implements PmTemplate {
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
        return "应用服务创建失败";
    }

    /**
     * 变量: projectName, appServiceName, projectId, projectCategory, organizationId
     */
    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下创建的应用服务“${appServiceName}”失败</p>\n" +
                "<p><a href=#/devops/app-service?type=project&id=${projectId}&name=${appServiceName}&category=${projectCategory}&organizationId=${organizationId}>查看详情</a></p>";
    }
}
