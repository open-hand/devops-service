package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DefaultConfigVO;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/project_config")
public class DevopsProjectConfigController {

    @Autowired
    DevopsConfigService devopsConfigService;

    /**
     * 项目下处理配置
     *
     * @param projectId       项目id
     * @param devopsConfigVOS 配置信息
     * @return void
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建配置")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody List<DevopsConfigVO> devopsConfigVOS) {
        devopsConfigService.operate(projectId, ResourceType.PROJECT.value(), devopsConfigVOS);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * 项目下查询配置详情
     * *
     *
     * @param projectId 项目id
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询配置详情")
    @CustomPageRequest
    @GetMapping
    public ResponseEntity<List<DevopsConfigVO>> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(
                devopsConfigService.queryByResourceId(projectId, ResourceType.PROJECT.value()))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.get.type"));
    }


    /**
     * 获取项目默认的配置
     *
     * @param projectId 项目id
     * @return ProjectDefaultConfigDTO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "获取项目默认的配置")
    @GetMapping("/default_config")
    public ResponseEntity<DefaultConfigVO> queryProjectDefaultConfig(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(
                devopsConfigService.queryDefaultConfig(projectId, ResourceType.PROJECT.value()))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.get"));
    }
}
