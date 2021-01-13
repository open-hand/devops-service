package io.choerodon.devops.infra.feign.fallback;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.test.ApiTestExecuteVO;
import io.choerodon.devops.infra.feign.TestServiceClient;

/**
 * @author zmf
 * @since 9/17/20
 */
@Component
public class TestServiceClientFallback implements TestServiceClient {

    @Override
    public ResponseEntity<String> executeTask(Long projectId, ApiTestExecuteVO apiTestExecuteVO, Long executorId, String triggerType, Long triggerId) {
        throw new CommonException("error.failed.to.execute.task");
    }

    @Override
    public ResponseEntity<String> queryById(Long projectId, Long recordId) {
        throw new CommonException("error.failed.to.query.test.record");
    }

    @Override
    public ResponseEntity<String> testConnection(String hostIp, Integer jmeterPort) {
        throw new CommonException("error.failed.to.test.host.connection");
    }
}
