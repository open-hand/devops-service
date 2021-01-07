package io.choerodon.devops.infra.feign;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.test.ApiTestExecuteVO;


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
    ResponseEntity<String> executeTask(
            @PathVariable("project_id") Long projectId,
            @RequestBody ApiTestExecuteVO apiTestExecuteVO,
            @RequestParam(value = "executor_id") Long executorId);

    @ApiOperation("根据纪录id查询纪录详情 / 不带有测试请求详情")
    @GetMapping("/v1/projects/{project_id}/api-test-records/{record_id}")
    ResponseEntity<String> queryById(@PathVariable("project_id") Long projectId,
                                     @PathVariable("record_id") Long recordId);

    @ApiOperation("测试jmeter服务器是够可用于分布式测试 / devops-service用")
    @GetMapping("/v1/distribute_hosts/connection_test")
    ResponseEntity<String> testConnection(@RequestParam("host_ip") String hostIp,
                                          @RequestParam("jmeter_port") Integer jmeterPort);
}
