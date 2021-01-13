package io.choerodon.devops.infra.feign.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.ServiceUnavailableException;
import io.choerodon.core.utils.FeignClientUtils;
import io.choerodon.devops.api.vo.test.ApiTestExecuteVO;
import io.choerodon.devops.api.vo.test.ApiTestTaskRecordVO;
import io.choerodon.devops.infra.dto.test.ApiTestTaskRecordDTO;
import io.choerodon.devops.infra.feign.TestServiceClient;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/9/14 9:28
 */
@Component
public class TestServiceClientOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestServiceClientOperator.class);
    @Autowired
    private TestServiceClient testServiceClient;

    /**
     * 执行api测试任务
     *
     * @param projectId
     * @param taskId
     * @param createdBy
     * @return
     */
    public ApiTestTaskRecordDTO executeTask(Long projectId, Long taskId, Long createdBy, String triggerType, Long triggerId) {
        ApiTestExecuteVO apiTestExecuteVO = new ApiTestExecuteVO();
        apiTestExecuteVO.setTaskId(taskId);
        return FeignClientUtils.doRequest(() -> testServiceClient.executeTask(projectId, apiTestExecuteVO, createdBy, triggerType, triggerId), ApiTestTaskRecordDTO.class, "error.execute.api.test.task");
    }

    /**
     * 查询api测试任务记录
     *
     * @param projectId
     * @param taskRecordId
     * @return
     */
    public ApiTestTaskRecordVO queryById(Long projectId, Long taskRecordId) {
        return FeignClientUtils.doRequest(() -> testServiceClient.queryById(projectId, taskRecordId), ApiTestTaskRecordVO.class, "error.query.api.test.task.record");
    }

    public boolean testJmeterConnection(String hostIp, Integer jmeterPort) {
        try {
            return FeignClientUtils.doRequest(() -> testServiceClient.testConnection(hostIp, jmeterPort), Boolean.class);
        } catch (ServiceUnavailableException ex) {
            LOGGER.debug("TestServiceClientOperator: Failed to test jmeter connection for host {} and port {}", hostIp, jmeterPort);
            LOGGER.debug("The ex is", ex);
            return false;
        }
    }
}
