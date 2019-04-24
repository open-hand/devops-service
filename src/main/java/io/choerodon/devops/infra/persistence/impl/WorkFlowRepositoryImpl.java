package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.WorkFlowRepository;
import io.choerodon.devops.infra.dataobject.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.feign.WorkFlowServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:56 2019/4/9
 * Description:
 */
@Component
public class WorkFlowRepositoryImpl implements WorkFlowRepository {


    private static final Logger LOGGER = LoggerFactory.getLogger(IamRepositoryImpl.class);

    private WorkFlowServiceClient workFlowServiceClient;

    public WorkFlowRepositoryImpl(WorkFlowServiceClient workFlowServiceClient) {
        this.workFlowServiceClient = workFlowServiceClient;
    }

    @Override
    public String create(Long projectId, DevopsPipelineDTO devopsPipelineDTO) {
        ResponseEntity<String> responseEntity = workFlowServiceClient.create(projectId, devopsPipelineDTO);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.workflow.create");
        }
        return responseEntity.getBody();
    }

    @Override
    public Boolean approveUserTask(Long projectId, Long instanceId) {
        ResponseEntity<Boolean> responseEntity = workFlowServiceClient.approveUserTask(projectId, instanceId);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.workflow.approve");
        }
        return responseEntity.getBody();
    }

    @Override
    public void stopInstance(Long projectId, Long pipelineRecordId) {
        ResponseEntity responseEntity = workFlowServiceClient.stopInstance(projectId, pipelineRecordId);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.workflow.stop");
        }
    }
}
