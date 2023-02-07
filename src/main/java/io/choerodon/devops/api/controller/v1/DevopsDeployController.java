package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.deploy.CustomDeployVO;
import io.choerodon.devops.api.vo.deploy.DockerDeployVO;
import io.choerodon.devops.api.vo.deploy.JarDeployVO;
import io.choerodon.devops.app.service.DevopsDockerInstanceService;
import io.choerodon.devops.app.service.DevopsHostAppService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Sheep on 2019/7/30.
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/deploy")
public class DevopsDeployController {

//    @Autowired
//    private DevopsDeployService devopsDeployService;
@Autowired
private DevopsDockerInstanceService devopsDockerInstanceService;
    @Autowired
    private DevopsHostAppService devopsHostAppService;

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

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "java部署")
    @PostMapping("/java")
    public ResponseEntity<Void> deployJavaInstance(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt @RequestBody @Validated JarDeployVO jarDeployVO) {
        devopsHostAppService.deployJavaInstance(projectId, jarDeployVO);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "其他类型部署")
    @PostMapping("/custom")
    public ResponseEntity<Void> deployCustomInstance(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt @RequestBody @Validated CustomDeployVO customDeployVO) {
        devopsHostAppService.deployCustomInstance(projectId, customDeployVO);
        return ResponseEntity.noContent().build();
    }

//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @ApiOperation(value = "hzero部署", hidden = true)
//    @PostMapping("/hzero")
//    public ResponseEntity<Long> deployHzeroApplication(
//            @ApiParam(value = "项目Id", required = true)
//            @PathVariable(value = "project_id") Long projectId,
//            @RequestBody @Validated HzeroDeployVO hzeroDeployVO) {
//        return ResponseEntity.ok(devopsDeployService.deployHzeroApplication(projectId, hzeroDeployVO));
//    }

}
