package io.choerodon.devops.infra.feign;

import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.devops.infra.feign.fallback.NotifyTransferDataClientFallback;

/**
 * User: Mr.Wang
 * Date: 2019/12/12
 */
@FeignClient(value = "notify-service", path = "/v1/upgrade", fallback = NotifyTransferDataClientFallback.class)
public interface NotifyTransferDataClient {

    @GetMapping
    ResponseEntity<String> checkLog(
            @ApiParam(value = "version")
            @RequestParam(value = "version") String version,
            @ApiParam(value = "type")
            @RequestParam(value = "type") String type);

}
