package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.deploy.DeployConfigVO;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.app.service.DevopsDeployService;
import io.choerodon.devops.app.service.DevopsDockerInstanceService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Sheep on 2019/7/30.
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/deploy")
public class DevopsDeployController {

    @Autowired
    private DevopsDeployService devopsDeployService;
    @Autowired
    private DevopsDockerInstanceService devopsDockerInstanceService;


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "主机部署")
    @PostMapping("/host")
    public ResponseEntity<Void> manualDeploy(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody @Validated DeployConfigVO deployConfigVO) {
        devopsDeployService.hostDeploy(projectId, deployConfigVO);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "镜像部署")
    @PostMapping("/docker")
    public ResponseEntity<Void> deployDockerInstance(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody @Validated DockerDeployVO dockerDeployVO) {
        devopsDockerInstanceService.deployDockerInstance(projectId, dockerDeployVO);
        return ResponseEntity.noContent().build();
    }

}
