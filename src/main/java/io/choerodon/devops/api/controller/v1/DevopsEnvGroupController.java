package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsEnvGroupVO;
import io.choerodon.devops.app.service.DevopsEnvGroupService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Creator: Runge
 * Date: 2018/9/4
 * Time: 14:18
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/env_groups")
public class DevopsEnvGroupController {

    @Autowired
    private DevopsEnvGroupService devopsEnvGroupService;

    /**
     * 项目下创建环境组
     *
     * @param projectId 项目id
     * @param name      环境组名称
     * @return DevopsEnvGroupVO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建环境组")
    @PostMapping
    public ResponseEntity<DevopsEnvGroupVO> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境组信息", required = true)
            @RequestParam String name) {
        return Optional.ofNullable(devopsEnvGroupService.create(name, projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.group.create"));
    }


    /**
     * 项目下更新环境组
     *
     * @param projectId        项目id
     * @param devopsEnvGroupVO 环境组信息
     * @return DevopsEnvGroupVO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新环境组")
    @PutMapping
    public ResponseEntity<DevopsEnvGroupVO> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境组信息", required = true)
            @RequestBody DevopsEnvGroupVO devopsEnvGroupVO) {
        return Optional.ofNullable(devopsEnvGroupService.update(devopsEnvGroupVO, projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.group.update"));
    }


    /**
     * 项目下查询环境组
     *
     * @param projectId 项目id
     * @return DevopsEnvGroupVO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询环境组")
    @GetMapping("/list_by_project")
    public ResponseEntity<List<DevopsEnvGroupVO>> listByProject(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(devopsEnvGroupService.listByProject(projectId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.group.get"));
    }

    /**
     * 校验环境组名唯一性
     *
     * @param projectId 项目id
     * @return boolean
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验环境组名唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境组名", required = true)
            @RequestParam String name,
            @ApiParam(value = "环境组id", required = false)
            @RequestParam(value = "group_id", required = false) Long groupId) {
        return Optional.ofNullable(devopsEnvGroupService.checkName(name, projectId, groupId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.env.group.get"));
    }


    /**
     * 环境组删除
     *
     * @param projectId 项目id
     * @param groupId   实例id
     * @return responseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "环境组删除")
    @DeleteMapping(value = "/{group_id}")
    public ResponseEntity delete(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境组ID", required = true)
            @PathVariable(value = "group_id") Long groupId) {
        devopsEnvGroupService.delete(groupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
