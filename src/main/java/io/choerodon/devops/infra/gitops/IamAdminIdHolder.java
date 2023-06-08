package io.choerodon.devops.infra.gitops;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

/**
 * 保存 iam admin 用户的id
 *
 * @author zmf
 * @since 2021/5/18
 */
public class IamAdminIdHolder {

    /**
     * 持有的admin id
     */
    private static Long adminId = null;

    /**
     * admin用户的登录名
     */
    public static final String IAM_ADMIN_LOGIN_NAME = "admin";

    private IamAdminIdHolder() {

    }

    public static Long getAdminId() {
        if (adminId == null) {
            // 不用加锁，多次调用，id也是一样的值
            adminId = ApplicationContextHelper
                    .getContext()
                    .getBean(BaseServiceClientOperator.class)
                    .queryUserByLoginName(IAM_ADMIN_LOGIN_NAME).getId();
        }

        return adminId;
    }
}
