package io.choerodon.devops.infra.feign;

import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.fallback.BaseServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Create by Li Jinyan on 2019/8/13
 */

@FeignClient(value = "base-service", fallback = BaseServiceClientFallback.class)
public interface BaseServiceClient {
    @GetMapping(value = "/v1/organizations/{organization_id}/applications/{id}/project")
    ResponseEntity<ProjectDTO> queryProjectByApp(@PathVariable("organization_id") Long organizationId,
                                                 @PathVariable("id") Long applicationId);
}
