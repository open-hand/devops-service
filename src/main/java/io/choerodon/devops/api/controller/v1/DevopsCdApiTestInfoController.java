package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.DevopsCdApiTestInfoService;

@RestController
@RequestMapping("/v1/projects/{project_id}/api_test_info")
public class DevopsCdApiTestInfoController {
    @Autowired
    private DevopsCdApiTestInfoService devopsCdApiTestInfoService;

    @ApiOperation("查询测试套件是否关联流水线")
    @GetMapping("/suites/{suite_id}/related_with_pipeline")
    public ResponseEntity<Boolean> doesApiTestSuiteRelatedWithPipeline(@ApiParam(value = "项目Id", required = true)
                                                                       @PathVariable(value = "project_id") Long projectId,
                                                                       @PathVariable(value = "suite_id") Long suiteId) {
        return Results.success(devopsCdApiTestInfoService.doesApiTestSuiteRelatedWithPipeline(suiteId));
    }
}
