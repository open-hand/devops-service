package io.choerodon.devops.infra.feign;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.SagaInstanceDetails;
import io.choerodon.devops.infra.feign.fallback.AsgardFeignClientFallback;


/**
 * @author dengyouquan
 **/
@FeignClient(value = "hzero-asgard",
        fallback = AsgardFeignClientFallback.class)
public interface AsgardFeignClient {

    @GetMapping("/v1/sagas/instances/ref/business/instance")
    ResponseEntity<List<SagaInstanceDetails>> queryByRefTypeAndRefIds(@RequestParam(value = "refType") String refType,
                                                                      @RequestParam(value = "refIds") List<String> refIds,
                                                                      @RequestParam(value = "sagaCode") String sagaCode);
}