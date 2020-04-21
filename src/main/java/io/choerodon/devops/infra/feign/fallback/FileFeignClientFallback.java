package io.choerodon.devops.infra.feign.fallback;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.FileDTO;
import io.choerodon.devops.infra.feign.FileFeignClient;

/**
 * @author superlee
 */
@Component
public class FileFeignClientFallback implements FileFeignClient {
    private static final String MSG_ERROR_UPLOAD = "error.file.upload";
    private static final String ERROR_FILE_DELETE = "error.file.delete";

    @Override
    public ResponseEntity<String> uploadFile(String bucketName, String fileName, MultipartFile multipartFile) {
        throw new CommonException(MSG_ERROR_UPLOAD);
    }

    @Override
    public ResponseEntity deleteFile(String bucketName, String url) {
        throw new CommonException(ERROR_FILE_DELETE, url);
    }

    @Override
    public ResponseEntity<FileDTO> upload(String bucketName, String fileName, MultipartFile multipartFile) {
        throw new CommonException(MSG_ERROR_UPLOAD);
    }

    @Override
    public ResponseEntity<String> cutImage(MultipartFile file, Double rotate, Integer axisX, Integer axisY, Integer width, Integer height) {
        return null;
    }
}
