package io.choerodon.devops.infra.feign.operator;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.UserAppServiceIdsVO;
import io.choerodon.devops.infra.feign.RducmClient;

@Component
public class RducmClientOperator {
    @Autowired
    private RducmClient rducmClient;

    /**
     * 查询用户在组织有所有有权限的应用服务
     */
    public UserAppServiceIdsVO getAppServiceIds(Long organizationId, Long userId) {
        ResponseEntity<UserAppServiceIdsVO> response = rducmClient.getAppServiceIds(Objects.requireNonNull(organizationId), Objects.requireNonNull(userId));
        if (response == null || response.getBody() == null) {
            throw new CommonException("error.list.app.id");
        }
        return response.getBody();
    }
}
