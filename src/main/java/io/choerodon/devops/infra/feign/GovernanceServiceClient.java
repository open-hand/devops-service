package io.choerodon.devops.infra.feign;

import io.choerodon.devops.infra.dto.governance.NacosListenConfigDTO;
import io.choerodon.devops.infra.feign.fallback.GovernanceServiceClientFallback;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Created by younger on 2018/3/29.
 */

@FeignClient(value = "governance-service", fallback = GovernanceServiceClientFallback.class)
public interface GovernanceServiceClient {
    @GetMapping(value = "/v1/{organization_id}/{project_id}/configs/listen_config")
    @ApiOperation(value = "按照项目Id查询项目的不可变信息")
    ResponseEntity<List<NacosListenConfigDTO>> batchQueryListenConfig(@PathVariable(name = "organization_id") Long organizationId,
                                                                      @PathVariable(name = "project_id") Long projectId,
                                                                      @RequestBody Set<Long> configIds);
}
