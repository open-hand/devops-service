package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;
import org.springframework.stereotype.Component;

/**
 * 删除应用服务的邮件模板
 *
 * @author zmf
 * @since 12/4/19
 */
@Component
public class DeleteAppServiceEmailTemplate implements EmailTemplate {

    @Override
    public String code() {
        return NoticeCodeConstants.DELETE_APP_SERVICE;
    }

    @Override
    public String name() {
        return "删除应用服务的站内信模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.DELETE_APP_SERVICE;
    }

    @Override
    public String title() {
        return "Choerodon通知";
    }

    /**
     * projectName, appServiceName
     */
    @Override
    public String content() {
        return "项目“${projectName}”下的应用服务“${appServiceName}”已被删除</p>";
    }
}
