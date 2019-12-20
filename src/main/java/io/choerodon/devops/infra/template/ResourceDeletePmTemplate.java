package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.Level;
import io.choerodon.core.notify.NotifyBusinessType;
import io.choerodon.core.notify.PmTemplate;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;
import org.springframework.stereotype.Component;

/**
 * @author zmf
 * @since 12/4/19
 */
@NotifyBusinessType(code = NoticeCodeConstants.RESOURC_EDELETE_CONFIRMATION,
        name = "资源删除验证通知", level = Level.PROJECT,
        description = "资源删除验证通知", isAllowConfig = false, isManualRetry = true, categoryCode = "resource-security-notice",
        pmEnabledFlag = true,
        emailEnabledFlag = true)
@Component
public class ResourceDeletePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "ResourceDeleteConfirmationPm";
    }

    @Override
    public String name() {
        return "资源删除验证站内信模板";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.RESOURC_EDELETE_CONFIRMATION;
    }

    @Override
    public String title() {
        return "资源删除验证";
    }

    @Override
    public String content() {
        return "<p>${user}正在${env}环境下执行删除${object}\"${objectName}\"的操作，验证码为：${captcha}；确认后，需将此验证码提供给操作者${user}完成删除操作。验证码10分钟内有效。</p>";
    }
}