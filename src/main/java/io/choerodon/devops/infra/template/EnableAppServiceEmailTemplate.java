package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 12/4/19
 */
@Component
public class EnableAppServiceEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "EnableAppServiceEmail";
    }

    @Override
    public String name() {
        return "启用应用服务邮件模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.APP_SERVICE_ENABLED;
    }

    @Override
    public String title() {
        return "Choerodon通知";
    }

    @Override
    public String content() {
        return "<p>项目“${projectName}”下的应用服务“${appServiceName}”已被启用</p>";
    }
}
