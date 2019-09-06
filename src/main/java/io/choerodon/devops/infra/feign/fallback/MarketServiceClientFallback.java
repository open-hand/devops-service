package io.choerodon.devops.infra.feign.fallback;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.feign.MarketServiceClient;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:31 2019/9/6
 * Description:
 */
@Component
public class MarketServiceClientFallback implements MarketServiceClient {
    @Override
    public ResponseEntity<Boolean> uploadFile(String appVersion, List<MultipartFile> files, String imageUrl) {
        throw new CommonException("error.upload.file.within");
    }

    @Override
    public ResponseEntity<Boolean> updateAppPublishInfoFix(String code, String version, String marketApplicationVOStr, List<MultipartFile> files, String imageUrl) {
        throw new CommonException("error.upload.file.fix.version.within");
    }
}
