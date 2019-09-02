package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.app.service.DevopsDeployValueService;
import io.choerodon.swagger.annotation.CustomPageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:56 2019/4/10
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/deploy_value")
public class DevopsDeployValueController {
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;

    /**
     * 项目下获取部署配置
     *
     * @param projectId   项目Id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return 部署配置
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下获取部署配置")
    @CustomPageRequest
    @PostMapping("/page_by_options")
    public ResponseEntity<PageInfo<DevopsDeployValueVO>> pageByOptions(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsDeployValueService.pageByOptions(projectId, appServiceId, envId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.list"));
    }

    /**
     * 项目下创建流水线配置
     *
     * @param projectId           项目Id
     * @param devopsDeployValueVO 配置信息
     * @return 部署配置
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建流水线配置")
    @PostMapping
    public ResponseEntity<DevopsDeployValueVO> createOrUpdate(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署配置相关信息")
            @RequestBody DevopsDeployValueVO devopsDeployValueVO) {
        return Optional.ofNullable(devopsDeployValueService.createOrUpdate(projectId, devopsDeployValueVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.createOrUpdate"));
    }

    /**
     * 项目下查询配置详情
     *
     * @param projectId 项目Id
     * @param valueId   配置Id
     * @return 配置信息
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询配置详情")
    @GetMapping
    public ResponseEntity<DevopsDeployValueVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置Id", required = true)
            @RequestParam(value = "value_id") Long valueId) {
        return Optional.ofNullable(devopsDeployValueService.query(projectId, valueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.queryById"));
    }

    /**
     * 项目下删除配置
     *
     * @param projectId 项目Id
     * @param valueId   配置Id
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下删除配置")
    @DeleteMapping
    public ResponseEntity delete(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置id", required = true)
            @RequestParam(value = "value_id") Long valueId) {
        devopsDeployValueService.delete(projectId, valueId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    /**
     * 名称校验
     *
     * @param projectId 项目id
     * @param name      名称
     * @return 没有内容则名称合法
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "名称校验")
    @GetMapping("/check_name")
    public ResponseEntity checkName(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "名称", required = true)
            @RequestParam(value = "name") String name) {
        devopsDeployValueService.checkName(projectId, name);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 检测能否删除
     *
     * @param projectId 项目id
     * @param valueId   配置id
     * @return true则可以删除
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检测能否删除")
    @GetMapping("/check_delete")
    public ResponseEntity<Boolean> checkDelete(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "valueId", required = true)
            @RequestParam(value = "value_id") Long valueId) {
        return Optional.ofNullable(devopsDeployValueService.checkDelete(projectId, valueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.check.delete"));
    }

    /**
     * 根据应用服务Id和环境Id获取配置
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @param envId        环境id
     * @return 配置信息
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据应用服务Id和环境Id获取配置")
    @GetMapping("/list_by_env_and_app")
    public ResponseEntity<List<DevopsDeployValueVO>> listByEnvAndApp(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用服务Id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @ApiParam(value = "环境Id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsDeployValueService.listByEnvAndApp(projectId, appServiceId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.queryByIds"));
    }
}
