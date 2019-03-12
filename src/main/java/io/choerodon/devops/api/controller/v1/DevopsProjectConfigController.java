package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.app.service.DevopsProjectConfigService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Optional;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/projectConfig")
public class DevopsProjectConfigController {

    @Autowired
    DevopsProjectConfigService devopsProjectConfigService;

    /**
     * 项目下创建配置
     *
     * @param projectId              项目id
     * @param devopsProjectConfigDTO 配置信息
     * @return ResponseEntity<DevopsProjectConfigDTO>
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建配置映射")
    @PostMapping
    public ResponseEntity<DevopsProjectConfigDTO> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody DevopsProjectConfigDTO devopsProjectConfigDTO) {
        return Optional.ofNullable(devopsProjectConfigService.create(projectId, devopsProjectConfigDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devopsProjectConfig.insert"));
    }

    /**
     * 项目下更新配置信息
     *
     * @param projectId              项目id
     * @param devopsProjectConfigDTO 配置信息
     * @return ResponseEntity<DevopsProjectConfigDTO>
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新应用信息")
    @PutMapping
    public ResponseEntity<DevopsProjectConfigDTO> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody DevopsProjectConfigDTO devopsProjectConfigDTO) {
        return Optional.ofNullable(devopsProjectConfigService.updateByPrimaryKeySelective(projectId, devopsProjectConfigDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devopsProjectConfig.update"));
    }

    /**
     * 项目下删除配置
     *
     * @param projectId       项目id
     * @param projectConfigId 配置id
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下删除配置")
    @DeleteMapping("/{projectConfigId}")
    public ResponseEntity deleteByProjectConfigId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "应用id", required = true)
            @PathVariable Long projectConfigId) {
        devopsProjectConfigService.delete(projectConfigId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下分页查询应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param param       param过滤参数
     * @return Page
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下分页查询配置")
    @CustomPageRequest
    @PostMapping("/list_by_options")
    public ResponseEntity<Page<DevopsProjectConfigDTO>> pageByOptions(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "过滤参数")
            @RequestBody(required = false) String param) {
        return Optional.ofNullable(
                devopsProjectConfigService.listByOptions(projectId, pageRequest, param))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devopsProjectConfig.get"));
    }

    /**
     * 项目下根据Id查询应用
     *
     * @param projectId       项目id
     * @param projectConfigId 配置id
     * @return ResponseEntity<DevopsProjectConfigDTO>
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下分页查询配置")
    @CustomPageRequest
    @GetMapping("/{project_configId}")
    public ResponseEntity<DevopsProjectConfigDTO> pageByOptions(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置Id", required = true)
            @PathVariable(value = "project_configId") Long projectConfigId) {
        return Optional.ofNullable(
                devopsProjectConfigService.queryByPrimaryKey(projectConfigId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.projectConfig.get"));
    }
}
