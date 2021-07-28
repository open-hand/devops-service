package io.choerodon.devops.infra.feign.operator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.deploy.hzero.HzeroDeployPipelineVO;
import io.choerodon.devops.infra.dto.workflow.DevopsPipelineDTO;
import io.choerodon.devops.infra.feign.WorkFlowServiceClient;
import io.choerodon.devops.infra.util.CustomContextUtil;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:11 2019/7/19
 * Description:
 */
@Component
public class WorkFlowServiceOperator {
    @Autowired
    private WorkFlowServiceClient workFlowServiceClient;

    public String create(Long projectId, DevopsPipelineDTO devopsPipelineDTO) {
        ResponseEntity<String> responseEntity = workFlowServiceClient.create(projectId, devopsPipelineDTO);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.workflow.create");
        }
        return responseEntity.getBody();
    }

    public Boolean approveUserTask(Long projectId, String businessKey) {
        ResponseEntity<Boolean> responseEntity = workFlowServiceClient.approveUserTask(projectId, businessKey);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.workflow.approve");
        }
        return responseEntity.getBody();
    }

    public void approveUserTask(Long projectId, String businessKey, String loginName, Long userId, Long orgId) {
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        CustomContextUtil.setUserContext(loginName, userId, orgId);
        approveUserTask(projectId, businessKey);
        // 复位用户上下文
        CustomContextUtil.setUserContext(userDetails);
    }


    public void stopInstance(Long projectId, String businessKey) {
        ResponseEntity<Void> responseEntity = workFlowServiceClient.stopInstance(projectId, businessKey);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.workflow.stop");
        }
    }

    public String createCiCdPipeline(Long projectId, DevopsPipelineDTO devopsPipelineDTO) {
        ResponseEntity<String> responseEntity = workFlowServiceClient.createCiCdPipeline(projectId, devopsPipelineDTO);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.workflow.create");
        }
        return responseEntity.getBody();
    }

    public String createHzeroPipeline(Long projectId, HzeroDeployPipelineVO hzeroDeployPipelineVO) {
        ResponseEntity<String> responseEntity = workFlowServiceClient.createHzeroPipeline(projectId, hzeroDeployPipelineVO);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.workflow.create");
        }
        return responseEntity.getBody();
    }
}
