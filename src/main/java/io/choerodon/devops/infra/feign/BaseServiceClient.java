package io.choerodon.devops.infra.feign;

import io.choerodon.devops.infra.dto.iam.ApplicationDTO;
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
    @GetMapping(value = "/v1/applications/{id}/project")
    ResponseEntity<ProjectDTO> queryProjectByApp(@PathVariable("id") Long id);

    @GetMapping(value = "/v1/applications/{id}")
    ResponseEntity<ApplicationDTO> getAppById(@PathVariable(value = "id")Long id);
}
