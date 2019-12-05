package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.PmTemplate;

/**
 * @author zmf
 * @since 12/4/19
 */
// TODO by zmf
public class InstanceFailurePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "InstanceFailurePm";
    }

    @Override
    public String name() {
        return "实例创建失败站内信模板";
    }

    @Override
    public String businessTypeCode() {
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return "实例创建失败";
    }

    @Override
    public String content() {
        return "<p>您在项目“${projectName}”下“${envName}”环境中创建的实例“${resourceName}”失败</p>";
    }
}
