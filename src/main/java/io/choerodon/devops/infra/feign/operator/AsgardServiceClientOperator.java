package io.choerodon.devops.infra.feign.operator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.exception.FeignException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.SagaInstanceDetails;
import io.choerodon.devops.infra.dto.asgard.QuartzTaskDTO;
import io.choerodon.devops.infra.dto.asgard.ScheduleTaskDTO;
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

    public QuartzTaskDTO createByServiceCodeAndMethodCode(Long organizationId, ScheduleTaskDTO scheduleTaskDTO) {
        ResponseEntity<QuartzTaskDTO> quartzTaskDTOResponseEntity = asgardFeignClient.createByServiceCodeAndMethodCode(ResourceLevel.ORGANIZATION.value(),
                organizationId,
                scheduleTaskDTO);
        QuartzTaskDTO quartzTaskDTO = quartzTaskDTOResponseEntity.getBody();
        if (quartzTaskDTOResponseEntity.getStatusCode().is2xxSuccessful() && quartzTaskDTO != null && quartzTaskDTO.getId() != null) {
            return quartzTaskDTOResponseEntity.getBody();
        } else {
            throw new CommonException("error.create.quartz.task");
        }
    }


    public QuartzTaskDTO queryByName(String taskName) {
        ResponseEntity<QuartzTaskDTO> quartzTaskDTOResponseEntity = asgardFeignClient.queryByName(taskName);
        return quartzTaskDTOResponseEntity.getBody();
    }

    public void deleteQuartzTask(List<Long> quartzTaskIds) {
        ResponseEntity<QuartzTaskDTO> roleResponseEntity = asgardFeignClient.deleteByIds(quartzTaskIds);
        if (!roleResponseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.delete.quartz.task");
        }
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
