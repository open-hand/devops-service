package io.choerodon.devops.infra.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import io.choerodon.devops.infra.config.FeignMultipartSupportConfig;
import io.choerodon.devops.infra.feign.fallback.MarketServiceClientFallback;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:31 2019/9/6
 * Description:
 */
@FeignClient(value = "market-service", fallback = MarketServiceClientFallback.class, configuration = FeignMultipartSupportConfig.class)
public interface MarketServiceClient {

    @PostMapping(value = "v1/market_applications/uploadWithin", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Boolean> uploadFile(@RequestParam(value = "app_version") String appVersion,
                                       @RequestPart Map map);

    @PutMapping("v1/market_applications/published/versionFixWithin")
    ResponseEntity<Boolean> updateAppPublishInfoFix(@RequestParam(value = "app_code") String code,
                                                    @RequestParam(value = "version") String version,
                                                    @RequestPart Map map);
}
