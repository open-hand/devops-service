package io.choerodon.devops.infra.util;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.IamAdminIdHolder;

public class GitUserNameUtil {

    private GitUserNameUtil() {
    }

    private static int adminId = -1;

    /**
     * 获得gitlab管理员id
     */
    public static int getAdminId() {
        if (adminId == -1) {
            synchronized (GitUserNameUtil.class) {
                if (adminId == -1) {
                    GitlabServiceClientOperator gitlabServiceClientOperator = (GitlabServiceClientOperator) ApplicationContextHelper.getContext().getBean("gitlabServiceClientOperator");
                    GitLabUserDTO gitLabUserDTO = gitlabServiceClientOperator.queryAdminUser();
                    adminId = gitLabUserDTO.getId();
                }
                return adminId;
            }
        } else {
            return adminId;
        }
    }

    /**
     * 获取登录用户名
     *
     * @return username
     */
    public static String getUsername() {
        CustomUserDetails details = DetailsHelper.getUserDetails();
        return getGitlabRealUsername(details.getUsername());
    }

    // TODO 调用方修改逻辑
    /**
     * 获取登录用户Id
     *
     * @return userId
     */
    public static Long getUserId() {
        CustomUserDetails details = DetailsHelper.getUserDetails();
        return details.getUserId();
    }

    public static String getEmail(){
        CustomUserDetails details = DetailsHelper.getUserDetails();
        return  details.getEmail();

    }


    /**
     * 获取登录用户名
     *
     * @return username
     */
    public static String getRealUsername() {
        CustomUserDetails details = DetailsHelper.getUserDetails();
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

    public static Long getIamUserIdByGitlabUserName(String username) {
        if ("admin1".equals(username) || "root".equals(username)) {
            return IamAdminIdHolder.getAdminId();
        }
        UserAttrService userAttrService = ApplicationContextHelper.getContext().getBean(UserAttrService.class);
        UserAttrDTO userAttrE = userAttrService.baseQueryByGitlabUserName(username);
        // 用户可能会在gitlab修改username，修改后devops这边查不到用户信息，此时应该去gitlab查，再更新devops数据
        if (userAttrE == null) {
            GitlabServiceClientOperator gitlabServiceClientOperator = ApplicationContextHelper.getContext().getBean(GitlabServiceClientOperator.class);
            GitLabUserDTO gitLabUserDTO = gitlabServiceClientOperator.queryUserByUserName(username);
            if (gitLabUserDTO == null) {
                return null;
            } else {
                userAttrE = userAttrService.baseQueryByGitlabUserId(gitLabUserDTO.getId().longValue());
                userAttrE.setGitlabUserName(username);
                userAttrService.baseUpdate(userAttrE);
            }
        }
        return userAttrE.getIamUserId();
    }
}
