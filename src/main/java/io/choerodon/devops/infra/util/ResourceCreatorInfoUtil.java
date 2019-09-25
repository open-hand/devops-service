package io.choerodon.devops.infra.util;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

/**
 * 资源创造者信息工具
 *
 * @author lihao
 * @date 2019-08-14 15:02
 */
public class ResourceCreatorInfoUtil {
    private ResourceCreatorInfoUtil() {

    }

    /**
     * 获得资源创造者的名称 登录名+真实名
     *
     * @param baseServiceClientOperator
     * @param userId
     * @return
     */
    public static String getOperatorName(BaseServiceClientOperator baseServiceClientOperator, Long userId) {
        IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByUserId(userId);
        if (iamUserDTO == null) {
            return null;
        }
        if (iamUserDTO.getLdap()) {
            return iamUserDTO.getLoginName() + " " + iamUserDTO.getRealName();
        } else {
            return iamUserDTO.getEmail() + " " + iamUserDTO.getRealName();
        }
    }
}
