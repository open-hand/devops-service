package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/23 10:44
 */
@RestController
@RequestMapping("/v1/fix")
public class DevopsDataFixController {
    @Autowired
    private DevopsCheckLogService devopsCheckLogService;

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "主机部署")
    @PostMapping("/v_1_2_pipeline_data_fix")
    public ResponseEntity<Void> manualDeploy() {
        devopsCheckLogService.devopsCiPipelineDataFix();
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "主机部署")
    @PostMapping("/v_1_2_pipeline_data_maven_publish_fix")
    public ResponseEntity<Void> mavenPublishFix() {
        devopsCheckLogService.pipelineDataMavenPublishFix();
        return ResponseEntity.noContent().build();
    }
}
