package io.choerodon.devops.infra.feign.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.test.ApiTestExecuteVO;
import io.choerodon.devops.api.vo.test.ApiTestTaskRecordVO;
import io.choerodon.devops.infra.dto.test.ApiTestTaskRecordDTO;
import io.choerodon.devops.infra.feign.TestServiceClient;

/**
 * @author zmf
 * @since 9/17/20
 */
public class TestServiceClientFallback implements TestServiceClient {
    @Override
    public ResponseEntity<ApiTestTaskRecordDTO> executeTask(Long projectId, ApiTestExecuteVO apiTestExecuteVO, Long executorId) {
        throw new CommonException("error.failed.to.execute.task");
    }

    @Override
    public ResponseEntity<ApiTestTaskRecordVO> queryById(Long projectId, Long recordId) {
        throw new CommonException("error.failed.to.query.test.record");
    }

    @Override
    public ResponseEntity<Boolean> testConnection(String hostIp, Integer jmeterPort) {
        return new ResponseEntity<>(false, HttpStatus.OK);
    }
}
