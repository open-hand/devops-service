package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.GitOpsConstants.NEW_LINE;

import org.hzero.core.base.BaseConstants;

/**
 * 帮助构建用户的错误信息
 *
 * @author zmf
 * @since 2021/1/21
 */
public class UserSyncErrorBuilder {
    private StringBuilder stringBuilder;

    public UserSyncErrorBuilder() {
        buildHeader();
    }

    private void buildHeader() {
        stringBuilder = new StringBuilder();
        stringBuilder.append("userId").append(BaseConstants.Symbol.COMMA).append("realName").append(BaseConstants.Symbol.COMMA).append("loginName").append(BaseConstants.Symbol.COMMA).append("errorMessage").append(NEW_LINE);
    }

    public UserSyncErrorBuilder addErrorUser(Long userId, String userRealName, String loginName, String errorMessage) {
        stringBuilder.append(userId)
                .append(BaseConstants.Symbol.COMMA)
                .append(userRealName)
                .append(BaseConstants.Symbol.COMMA)
                .append(loginName)
                .append(BaseConstants.Symbol.COMMA)
                .append(errorMessage)
                .append(NEW_LINE);
        return this;
    }

    public String build() {
        return stringBuilder.toString();
    }
}
