package io.choerodon.devops.api.controller.v1;

import static io.choerodon.devops.app.service.impl.DevopsCheckLogServiceImpl.MIGRATION_CD_PIPELINE_DATE;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping("/data_fix")
public class DataFixController {

    @Autowired
    private DevopsCheckLogService devopsCheckLogService;

    /**
     * 释放这个微服务实例所持有的一些资源(比如redis的键)
     * 一般由外部的容器根据生命周期调用
     */
    @PostMapping("/migration_cdpipeline_data")
    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "释放这个微服务实例所持有的一些资源(比如redis的键),一般由外部的容器根据生命周期调用")
    public ResponseEntity<Void> migrationCdPipelineDate() {
        devopsCheckLogService.checkLog(MIGRATION_CD_PIPELINE_DATE);
        return ResponseEntity.ok().build();
    }
}
