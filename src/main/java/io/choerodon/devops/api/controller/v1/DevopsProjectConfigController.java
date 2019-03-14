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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Optional;

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
                .orElseThrow(() -> new CommonException("error.devops.project.config.create"));
    }

    /**
     * 项目下更新配置信息
     *
     * @param projectId              项目id
     * @param devopsProjectConfigDTO 配置信息
     * @return ResponseEntity<DevopsProjectConfigDTO>
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
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
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
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
                .orElseThrow(() -> new CommonException("error.devops.project.config.list.by.options.get"));
    }

    /**
     * 项目下根据Id查询配置
     *
     * @param projectId       项目id
     * @param projectConfigId 配置id
     * @return ResponseEntity<DevopsProjectConfigDTO>
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下根据配置Id查询配置")
    @GetMapping("/{project_config_id}")
    public ResponseEntity<DevopsProjectConfigDTO> pageByOptions(
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
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下根据类型查询配置")
    @CustomPageRequest
    @GetMapping("/type")
    public ResponseEntity<List<DevopsProjectConfigDTO>> queryByIdAndType(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置类型", required = true)
            @RequestParam(value = "type") String type) {
        return Optional.ofNullable(
                devopsProjectConfigService.queryByIdAndType(projectId,type))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.get.type"));
    }
}
