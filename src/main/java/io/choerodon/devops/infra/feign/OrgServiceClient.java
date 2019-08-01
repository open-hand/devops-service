package io.choerodon.devops.infra.feign;

import com.github.pagehelper.PageInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.constant.PageConstant;
import io.choerodon.devops.api.vo.kubernetes.ProjectCategoryEDTO;
import io.choerodon.devops.infra.dto.iam.ProjectCategoryDTO;
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


    @GetMapping("/v1/organizations/{organization_id}/categories/list")
    ResponseEntity<PageInfo<ProjectCategoryDTO>> getProjectCategoryList(@PathVariable(name = "organization_id") Long organizationId,
                                                                        @RequestParam(name = "page", defaultValue = PageConstant.PAGE, required = false) final int page,
                                                                        @RequestParam(name = "size", defaultValue = PageConstant.SIZE, required = false) final int size,
                                                                        @RequestParam(name = "param", required = false) final String param);


}
