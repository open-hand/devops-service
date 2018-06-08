package io.choerodon.devops.infra.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;

public class GitUserNameUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitUserNameUtil.class);

    private GitUserNameUtil() {
    }

    /**
     * 获取登录用户名
     *
     * @return username
     */
    public static String getUsername() {
        CustomUserDetails details = DetailsHelper.getUserDetails();
        String username = getGitlabRealUsername(details.getUsername());
        LOGGER.info(String.format("=====%s", username));
        return username;
    }

    /**
     * 获取登录用户Id
     *
     * @return userId
     */
    public static Integer getUserId() {
        CustomUserDetails details = DetailsHelper.getUserDetails();
        Long userId = details.getUserId();
        LOGGER.info(String.format("=====%s", userId));
        return TypeUtil.objToInteger(userId);
    }


    /**
     * 获取登录用户名
     *
     * @return username
     */
    public static String getRealUsername() {
        CustomUserDetails details = DetailsHelper.getUserDetails();
        LOGGER.info(String.format("=====%s", details.getUsername()));
        return details.getUsername();
    }

    public static Long getOrganizationId() {
        CustomUserDetails details = DetailsHelper.getUserDetails();
        return details.getOrganizationId();
    }

    private static String getGitlabRealUsername(String username) {
        //现在框架组那边获取UserDetail返回的name是realName,所以暂时用管理员匹配
        if ("admin".equals(username)) {
            return "admin1";
        }
        return username;
    }
}
