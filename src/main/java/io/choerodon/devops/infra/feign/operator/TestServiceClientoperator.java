package io.choerodon.devops.infra.feign.operator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
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
public class TestServiceClientoperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestServiceClientoperator.class);
    @Autowired
    private TestServiceClient testServiceClient;

    /**
     * 执行api测试任务
     *
     * @param projectId
     * @param taskId
     * @return
     */
    public ApiTestTaskRecordDTO executeTask(Long projectId, Long taskId) {
        ApiTestExecuteVO apiTestExecuteVO = new ApiTestExecuteVO();
        apiTestExecuteVO.setTaskId(taskId);
        ResponseEntity<ApiTestTaskRecordDTO> entity = testServiceClient.executeTask(projectId, apiTestExecuteVO);

        if (entity != null && !entity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.execute.api.test.task");
        }
        return entity == null ? null : entity.getBody();
    }

    /**
     * 查询api测试任务记录
     *
     * @param projectId
     * @param taskRecordId
     * @return
     */
    public ApiTestTaskRecordVO queryById(Long projectId, Long taskRecordId) {

        ResponseEntity<ApiTestTaskRecordVO> entity = testServiceClient.queryById(projectId, taskRecordId);

        if (entity != null && !entity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.query.api.test.task.record");
        }
        return entity == null ? null : entity.getBody();
    }
}
