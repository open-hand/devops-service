package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.validator.EnvironmentApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/env/apps")
public class DevopsEnvApplicationController {

    @Autowired
    private DevopsEnvApplicationService devopsEnvApplicationService;

    @Autowired
    private EnvironmentApplicationValidator validator;

    /**
     * 创建环境下的应用关联
     *
     * @param devopsEnvApplicationCreationVO 环境和应用的关联关系
     * @return ApplicationRepDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "创建环境下的应用关联")
    @PostMapping("/batch_create")
    public ResponseEntity<List<DevopsEnvApplicationVO>> batch_create(
            @ApiParam(value = "关联信息", required = true)
            @RequestBody DevopsEnvApplicationCreationVO devopsEnvApplicationCreationVO) {
        validator.checkEnvIdExist(devopsEnvApplicationCreationVO.getEnvId());
        validator.checkAppIdsExist(devopsEnvApplicationCreationVO.getAppIds());
        return Optional.ofNullable(devopsEnvApplicationService.batchCreate(devopsEnvApplicationCreationVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.app.create"));
    }

    /**
     * 查询环境下的所有应用
     *
     * @param envId 环境id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询环境下的所有应用")
    @GetMapping("/list_by_env")
    public ResponseEntity<List<ApplicationRepVO>> listAppByEnvId(
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsEnvApplicationService.listAppByEnvId(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.app.query"));
    }

    /**
     * 查询应用在环境下的所有label
     *
     * @param envId 环境id
     * @param appId 应用id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询应用在环境下的所有label")
    @GetMapping("/list_label")
    public ResponseEntity<List<DevopsEnvLabelVO>> listLabelByAppAndEnvId(
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "应用id", required = true)
            @RequestParam(value = "app_id") Long appId) {
        return Optional.ofNullable(devopsEnvApplicationService.listLabelByAppAndEnvId(envId, appId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.app.label.query"));
    }


    /**
     * 查询应用在环境下的所有端口
     *
     * @param envId 环境id
     * @param appId 应用id
     * @return List
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询应用在环境下的所有port")
    @GetMapping("/list_port")
    public ResponseEntity<List<DevopsEnvPortVO>> listPortByAppAndEnvId(
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "应用id", required = true)
            @RequestParam(value = "app_id") Long appId) {
        return Optional.ofNullable(devopsEnvApplicationService.listPortByAppAndEnvId(envId, appId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.app.port.query"));
    }
}
