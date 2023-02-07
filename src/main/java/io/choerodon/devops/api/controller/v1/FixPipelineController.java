package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/fix_pipeline")
public class FixPipelineController {

    @Autowired
    private DevopsCheckLogService devopsCheckLogService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("修复流水线")
    @PostMapping("/{pipeline_id}")
    public void fixPipeline(@PathVariable("pipeline_id") Long pipelineId) {
        devopsCheckLogService.fixPipeline(pipelineId);
    }
}
