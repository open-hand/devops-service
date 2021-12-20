package io.choerodon.devops.infra.feign;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.test.ApiTestExecuteVO;
import io.choerodon.swagger.annotation.Permission;


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
            @RequestParam(value = "executor_id") Long executorId,
            @RequestParam(value = "trigger_type") String triggerType,
            @RequestParam(value = "trigger_id") Long triggerId);

    @ApiOperation("执行测试套件")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/v1/projects/{project_id}/api_test/suites/{id}/execute")
    ResponseEntity<String> executeSuite(@ApiParam(value = "项目id", required = true)
                                        @PathVariable("project_id") Long projectId,
                                        @Encrypt @PathVariable("id") Long apiTestSuiteId,
                                        @ApiParam(value = "执行者id/服务内部调用需要")
                                        @RequestParam(value = "executor_id", required = false) Long executorId,
                                        @ApiParam(value = "触发类型 / 默认为手动触发")
                                        @RequestParam(value = "trigger_type", required = false, defaultValue = "manual") String triggerType,
                                        @ApiParam(value = "触发id / 可不传")
                                        @RequestParam(value = "trigger_id", required = false) Long triggerId);

    @ApiOperation("根据纪录id查询纪录详情 / 不带有测试请求详情")
    @GetMapping("/v1/projects/{project_id}/api-test-records/{record_id}")
    ResponseEntity<String> queryById(@PathVariable("project_id") Long projectId,
                                     @PathVariable("record_id") Long recordId);

    @ApiOperation("测试jmeter服务器是够可用于分布式测试 / devops-service用")
    @GetMapping("/v1/distribute_hosts/connection_test")
    ResponseEntity<String> testConnection(@RequestParam("host_ip") String hostIp,
                                          @RequestParam("jmeter_port") Integer jmeterPort);
}
