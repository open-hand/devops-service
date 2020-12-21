package io.choerodon.devops.infra.feign;

import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.choerodon.devops.api.vo.UserAppServiceIdsVO;
import io.choerodon.devops.infra.feign.fallback.RducmClientFallBack;

/**
 * User: Mr.Wang
 * Date: 2020/6/11
 */
@FeignClient(value = "code-repo-service", fallback = RducmClientFallBack.class)
public interface RducmClient {
    @GetMapping("/v1/organizations/{organizationId}/gitlab/developer/{userId}/repositories/within")
    ResponseEntity<UserAppServiceIdsVO> getAppServiceIds(
            @ApiParam(value = "组织id", required = true)
            @PathVariable(name = "organizationId") Long organizationId,
            @ApiParam(value = "用户id", required = true)
            @PathVariable(name = "userId") Long userId
    );
}
