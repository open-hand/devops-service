package io.choerodon.devops.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.feign.NotifyTransferDataClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * User: Mr.Wang
 * Date: 2019/12/12
 */
@Component
public class NotifyTransferDataClientFallback implements NotifyTransferDataClient {
    @Override
    public ResponseEntity<String> checkLog(String version, String type) {
        throw new CommonException("error.transfer.data");
    }
}
