package io.choerodon.devops.infra.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.infra.feign.fallback.GitlabServiceClientFallback;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:31 2019/9/6
 * Description:
 */
@FeignClient(value = "market-service", fallback = GitlabServiceClientFallback.class)
public interface MarketServiceClient {

    @PostMapping("market/v1/market_applications/uploadWithin")
    ResponseEntity<Boolean> uploadFile(@RequestParam(value = "app_version") String appVersion,
                                       @RequestPart List<MultipartFile> files,
                                       @RequestParam(required = false, value = "imageUrl") String imageUrl);

    @PutMapping("market/v1/market_applications/published/versionFixWithin")
    ResponseEntity<Boolean> updateAppPublishInfoFix(@RequestParam(value = "app_code") String code,
                                                    @RequestParam(value = "version") String version,
                                                    @RequestParam(value = "marketApplicationVOStr") String marketApplicationVOStr,
                                                    @RequestPart List<MultipartFile> files,
                                                    @RequestParam(required = false, value = "imageUrl") String imageUrl);
}
