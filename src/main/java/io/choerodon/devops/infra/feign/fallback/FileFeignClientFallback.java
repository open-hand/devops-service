package io.choerodon.devops.infra.feign.fallback;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.feign.FileFeignClient;

/**
 * @author superlee
 */
@Component
public class FileFeignClientFallback implements FileFeignClient {

    @Override
    public ResponseEntity<String> uploadFile(Long organizationId, String backetName, String fileName, MultipartFile multipartFile) {
        throw new CommonException("error.file.upload");
    }
}
