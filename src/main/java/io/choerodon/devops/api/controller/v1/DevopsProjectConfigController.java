package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.api.dto.ProjectDefaultConfigDTO;
import io.choerodon.devops.app.service.DevopsProjectConfigService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/project_config")
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
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建配置映射")
    @PostMapping
    public ResponseEntity<DevopsProjectConfigDTO> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody DevopsProjectConfigDTO devopsProjectConfigDTO) {
        return Optional.ofNullable(devopsProjectConfigService.create(projectId, devopsProjectConfigDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.create"));
    }

    /**
     * 创建配置校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      配置name
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "创建配置校验名称是否存在")
    @GetMapping(value = "/check_name")
    public ResponseEntity checkName(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置名", required = true)
            @RequestParam String name) {
        devopsProjectConfigService.checkName(projectId, name);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 项目下更新配置信息
     *
     * @param projectId              项目id
     * @param devopsProjectConfigDTO 配置信息
     * @return ResponseEntity<DevopsProjectConfigDTO>
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新配置信息")
    @PutMapping
    public ResponseEntity<DevopsProjectConfigDTO> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody DevopsProjectConfigDTO devopsProjectConfigDTO) {
        return Optional.ofNullable(devopsProjectConfigService.updateByPrimaryKeySelective(projectId, devopsProjectConfigDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.update"));
    }

    /**
     * 项目下删除配置
     *
     * @param projectId       项目id
     * @param projectConfigId 配置id
     * @return ResponseEntity
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下删除配置")
    @DeleteMapping("/{project_config_id}")
    public ResponseEntity deleteByProjectConfigId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置id", required = true)
            @PathVariable(value = "project_config_id") Long projectConfigId) {
        devopsProjectConfigService.delete(projectConfigId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下分页查询配置
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param param       param过滤参数
     * @return Page
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
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
                .orElseThrow(() -> new CommonException("error.devops.project.config.list.by.options.get"));
    }

    /**
     * 项目下根据Id查询配置
     *
     * @param projectId       项目id
     * @param projectConfigId 配置id
     * @return ResponseEntity<DevopsProjectConfigDTO>
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下根据配置Id查询配置")
    @GetMapping("/{project_config_id}")
    public ResponseEntity<DevopsProjectConfigDTO> queryByPrimaryKey(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置Id", required = true)
            @PathVariable(value = "project_config_id") Long projectConfigId) {
        return Optional.ofNullable(
                devopsProjectConfigService.queryByPrimaryKey(projectConfigId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.get"));
    }

    /**
     * 根据项目Id和类型查询配置
     *
     * @param projectId 项目id
     * @param type      配置类型
     * @return
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下根据类型查询配置")
    @CustomPageRequest
    @GetMapping("/type")
    public ResponseEntity<List<DevopsProjectConfigDTO>> queryByIdAndType(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置类型", required = true)
            @RequestParam(value = "type") String type) {
        return Optional.ofNullable(
                devopsProjectConfigService.queryByIdAndType(projectId, type))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.get.type"));
    }

    /**
     * 根据配置id查询，该配置是否被使用
     *
     * @param projectId 项目id
     * @return
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "根据配置Id检测该配置是否被使用")
    @CustomPageRequest
    @GetMapping("/{project_config_id}/check")
    public ResponseEntity<Boolean> checkIsUsed(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置Id", required = true)
            @PathVariable(value = "project_config_id") Long configId) {
        return Optional.ofNullable(
                devopsProjectConfigService.checkIsUsed(configId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.check.is.used"));
    }



    /**
     * 设置项目对应harbor仓库为私有或者公有
     *
     * @param projectId 项目id
     * @param harborPrivate  是否私有
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "设置项目对应harbor仓库为私有或者公有")
    @GetMapping("/enableProject")
    public ResponseEntity enableProject(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境 ID", required = true)
            @RequestParam boolean  harborPrivate) {
        devopsProjectConfigService.setHarborProjectIsPrivate(projectId,harborPrivate);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * 设置项目对应harbor仓库为私有或者公有
     *
     * @param projectId 项目id
     * @return ProjectDefaultConfigDTO
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "设置项目对应harbor仓库为私有或者公有")
    @GetMapping("/defaultConfig")
    public ResponseEntity<ProjectDefaultConfigDTO> getProjectDefaultConfig(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(
                devopsProjectConfigService.getProjectDefaultConfig(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.get"));
    }
}
