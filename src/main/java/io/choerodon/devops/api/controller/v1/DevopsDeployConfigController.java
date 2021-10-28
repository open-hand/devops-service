package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.deploy.ConfigSettingVO;
import io.choerodon.devops.app.service.DeployConfigService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by jianzhang on 2021/10/28.
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/deploy_config")
public class DevopsDeployConfigController {

    private DeployConfigService deployConfigService;

    public DevopsDeployConfigController(DeployConfigService deployConfigService){
        this.deployConfigService = deployConfigService;
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询配置文件信息")
    @GetMapping
    public ResponseEntity<ConfigSettingVO> queryDeployConfigByRecordId(
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt @RequestParam(value = "record_id", required = false) Long recordId,
            @Encrypt @RequestParam(value = "instance_id", required = false) Long instanceId) {
        return ResponseEntity.ok(deployConfigService.queryDeployConfig(projectId, recordId, instanceId));
    }
}
