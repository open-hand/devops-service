package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;
import org.springframework.stereotype.Component;

/**
 * User: Mr.Wang
 * Date: 2019/12/18
 */
@Component
public class ResourceDeleteEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "ResourceDeleteConfirmationEmail";
    }

    @Override
    public String name() {
        return "资源删除验证邮件模板";
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
        return "<p>${user}正在${env}环境下执行删除${object}\"${objectName}\"的操作，验证码为：${captcha}；确认后，需将此验证码提供给操作者${user}完成删除操作。验证码11分钟内有效。</p>";
    }
}