package io.choerodon.devops.infra.feign;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.choerodon.devops.api.vo.test.ApiTestExecuteVO;
import io.choerodon.devops.api.vo.test.ApiTestTaskRecordVO;
import io.choerodon.devops.infra.dto.test.ApiTestTaskRecordDTO;


/**
 * Created by Sheep on 2019/5/15.
 */

@FeignClient(value = "test-manager-service")
public interface TestServiceClient {

    /**
     * 执行指定任务
     *
     * @param projectId        项目id
     * @param apiTestExecuteVO 执行所需信息
     */
    @ApiOperation(value = "执行任务或者自选用例")
    @PostMapping(value = "/v1/projects/{project_id}/api_test/tasks/execute")
    ResponseEntity<ApiTestTaskRecordDTO> executeTask(
            @PathVariable("project_id") Long projectId,
            @RequestBody ApiTestExecuteVO apiTestExecuteVO);

    @ApiOperation("根据纪录id查询纪录详情 / 不带有测试请求详情")
    @GetMapping("/v1/projects/{project_id}/api-test-records/{record_id}")
    ResponseEntity<ApiTestTaskRecordVO> queryById(@PathVariable("project_id") Long projectId,
                                                  @PathVariable("record_id") Long recordId);
}
