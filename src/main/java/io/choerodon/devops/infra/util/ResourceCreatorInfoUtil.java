package io.choerodon.devops.infra.util;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;

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
     * @param iamServiceClientOperator
     * @param userId
     * @return
     */
    public static String getOperatorName(IamServiceClientOperator iamServiceClientOperator, Long userId) {
        IamUserDTO iamUserDTO = iamServiceClientOperator.queryUserByUserId(userId);
        return iamUserDTO.getLoginName() + " " + iamUserDTO.getRealName();
    }
}
