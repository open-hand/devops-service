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
     * 变量: projectCode, projectName, projectCategory, organizationId, appServiceCode
     */
    @Override
    public String content() {
        return "<p>您在项目“${projectCode}”下创建的应用服务“${appServiceCode}”失败</p>\n" +
                "<p><a href=#/devops/app-service?type=project&id=${projectId}&name=${projectName}&category=${projectCategory}&organizationId=${organizationId}>查看详情</a></p>";
    }
}
