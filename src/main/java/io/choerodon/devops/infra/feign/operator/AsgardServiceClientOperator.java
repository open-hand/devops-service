package io.choerodon.devops.infra.feign.operator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.devops.api.vo.SagaInstanceDetails;
import io.choerodon.devops.infra.feign.AsgardFeignClient;

@Component
public class AsgardServiceClientOperator {

    @Autowired
    private AsgardFeignClient asgardFeignClient;

    public List<SagaInstanceDetails> queryByRefTypeAndRefIds(String refType, List<String> refIds, String sagaCode) {
        ResponseEntity<List<SagaInstanceDetails>> listResponseEntity;
        try {
            listResponseEntity = asgardFeignClient.queryByRefTypeAndRefIds(refType, refIds, sagaCode);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        if (listResponseEntity == null) {
            throw new CommonException("devops.query.saga");
        }
        return listResponseEntity.getBody();
    }

    public void retrySaga(Long projectId, Long instanceId) {
        ResponseEntity<Void> voidResponseEntity;
        try {
            voidResponseEntity = asgardFeignClient.retry(projectId, instanceId);
            if (!voidResponseEntity.getStatusCode().is2xxSuccessful()) {
                throw new CommonException("devops.retry.saga.instance");
            }
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }
}
