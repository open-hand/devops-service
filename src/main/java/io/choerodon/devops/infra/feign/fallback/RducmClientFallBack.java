package io.choerodon.devops.infra.feign.fallback;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.UserAppServiceIdsVO;
import io.choerodon.devops.infra.feign.RducmClient;

@Component
public class RducmClientFallBack implements RducmClient {
    @Override
    public ResponseEntity<UserAppServiceIdsVO> getAppServiceIds(Long organizationId, Long userId) {
        throw new CommonException("error.list.app.id");
    }
}
