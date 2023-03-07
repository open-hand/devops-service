package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.SagaInstanceDetails;
import io.choerodon.devops.infra.dto.asgard.QuartzTaskDTO;
import io.choerodon.devops.infra.dto.asgard.ScheduleTaskDTO;
import io.choerodon.devops.infra.feign.AsgardFeignClient;

/**
 * @author dengyouquan
 **/
@Component
public class AsgardFeignClientFallback implements AsgardFeignClient {

    @Override
    public ResponseEntity<QuartzTaskDTO> createByServiceCodeAndMethodCode(String sourceLevel, Long sourceId, @Valid ScheduleTaskDTO dto) {
        throw new CommonException("error.create.quartz.task");
    }

    @Override
    public ResponseEntity<QuartzTaskDTO> queryByName(String taskName) {
        throw new CommonException("error.query.quartz.task");
    }

    @Override
    public ResponseEntity<QuartzTaskDTO> deleteByIds(List<Long> ids) {
        throw new CommonException("error.delete.quartz.task");
    }

    @Override
    public ResponseEntity<List<SagaInstanceDetails>> queryByRefTypeAndRefIds(String refType, List<String> refIds, String sagaCode) {
        throw new CommonException("devops.query.instance.detail");
    }

    @Override
    public ResponseEntity<Void> retry(Long projectId, Long instanceId) {
        throw new CommonException("devops.retry.saga.instance");
    }
}