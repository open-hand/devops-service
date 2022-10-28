package io.choerodon.devops.infra.feign.operator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.utils.FeignClientUtils;
import io.choerodon.devops.api.vo.test.ApiTestExecuteVO;
import io.choerodon.devops.api.vo.test.ApiTestSuiteRecordSimpleVO;
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
    public ApiTestTaskRecordDTO executeTask(Long projectId, Long taskId, Long createdBy, String triggerType, Long triggerId, Long getApITestConfigId) {
        ApiTestExecuteVO apiTestExecuteVO = new ApiTestExecuteVO();
        apiTestExecuteVO.setTaskId(taskId);
        apiTestExecuteVO.setConfigId(getApITestConfigId);
        return FeignClientUtils.doRequest(() -> testServiceClient.executeTask(projectId, apiTestExecuteVO, createdBy, triggerType, triggerId), ApiTestTaskRecordDTO.class, "devops.execute.api.test.task");
    }

    /**
     * 执行api测试任务
     *
     * @param projectId
     * @param suiteId
     * @param createdBy
     * @return
     */
    public ApiTestTaskRecordDTO executeSuite(Long projectId, Long suiteId, Long createdBy, String triggerType, Long triggerId) {
        return FeignClientUtils.doRequest(() -> testServiceClient.executeSuite(projectId,
                suiteId,
                createdBy,
                triggerType,
                triggerId), ApiTestTaskRecordDTO.class, "devops.execute.api.test.suite");
    }

    /**
     * 查询api测试任务记录
     *
     * @param projectId
     * @param taskRecordId
     * @return
     */
    public ApiTestTaskRecordVO queryById(Long projectId, Long taskRecordId) {
        return FeignClientUtils.doRequest(() -> testServiceClient.queryById(projectId, taskRecordId), ApiTestTaskRecordVO.class, "devops.query.api.test.task.record");
    }

    /**
     * 查询api测试任务记录
     *
     * @param projectId
     * @param taskRecordId
     * @return
     */
    public ApiTestSuiteRecordSimpleVO querySuitePreviewById(Long projectId, Long taskRecordId) {
        return FeignClientUtils.doRequest(() -> testServiceClient.querySuitePreviewById(projectId, taskRecordId),
                ApiTestSuiteRecordSimpleVO.class,
                "devops.query.api.test.task.record");
    }
}
