package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;

/**
 * @author zmf
 * @since 2/3/20
 */
@NotifyBusinessType(code = NoticeCodeConstants.GITLAB_PWD,
        name = "GitLab默认密码", level = Level.SITE,
        description = "GitLab默认密码", isManualRetry = true, categoryCode = "account-security-notice",
        pmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        isAllowConfig = false,
        targetUserType = {TargetUserType.TARGET_USER_SPECIFIER})
@Component
public class GitlabPasswordPmTemplate implements PmTemplate {

    @Override
    public String code() {
        return "GitlabPasswordPmTemplate";
    }

    @Override
    public String name() {
        return "GitLab默认密码";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.GITLAB_PWD;
    }

    @Override
    public String title() {
        return "GitLab默认密码";
    }

    /**
     * gitlabPassword GitLab默认密码
     * organizationId 组织id
     */
    @Override
    public String content() {
        return "您的GitLab仓库密码为: ${gitlabPassword}。为了您的账户安全，请尽快前往Choerodon<a href='#/base/user-info?type=site&organizationId=${organizationId}'>个人信息</a>页面修改默认的GitLab密码";
    }
}
