package io.choerodon.devops.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.choerodon.devops.domain.application.valueobject.ProjectCategoryEDTO;
import io.choerodon.devops.infra.feign.fallback.OrgServiceClientFallBack;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:48 2019/6/24
 * Description:
 */
@FeignClient(value = "organization-service", fallback = OrgServiceClientFallBack.class)
public interface OrgServiceClient {
    @PostMapping("/v1/organizations/{organization_id}/categories/create")
    ResponseEntity<ProjectCategoryEDTO> createProjectCategory(@PathVariable(name = "organization_id") Long organizationId,
                                                              @RequestBody ProjectCategoryEDTO createDTO);

}
