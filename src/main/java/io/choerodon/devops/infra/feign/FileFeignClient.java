package io.choerodon.devops.infra.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.infra.config.MultipartSupportConfig;
import io.choerodon.devops.infra.feign.fallback.FileFeignClientFallback;

/**
 * @author superlee
 */
@FeignClient(value = "file-service",
        configuration = MultipartSupportConfig.class,
        fallback = FileFeignClientFallback.class)
public interface FileFeignClient {

    @PostMapping(
            value = "/v1/organization/{organizationId}/file/backetName/{backetName}",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadFile(@PathVariable("organizationId") Long organizationId,
                                      @PathVariable("backetName") String backetName,
                                      @RequestParam("fileName") String fileName,
                                      @RequestPart("file") MultipartFile multipartFile);
}
