package io.choerodon.devops.api.controller.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/v1/data_fix")
public class DataFixController {

    @Autowired
    private DevopsCheckLogService devopsCheckLogService;

    @PostMapping("/migration_cdpipeline_data")
    @Permission(level = ResourceLevel.SITE)
    public ResponseEntity<Void> migrationCdPipelineDate() {
        devopsCheckLogService.migrationCdPipelineDate();
        return ResponseEntity.ok().build();
    }
}
