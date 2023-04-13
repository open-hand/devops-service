package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsEnvGroupVO;
import io.choerodon.devops.app.service.DevopsEnvGroupService;
import io.choerodon.swagger.annotation.Permission;


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
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建环境组")
    @PostMapping
    public ResponseEntity<DevopsEnvGroupVO> create(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境组信息", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(devopsEnvGroupService.create(name, projectId));
    }


    /**
     * 项目下更新环境组
     *
     * @param projectId        项目id
     * @param devopsEnvGroupVO 环境组信息
     * @return DevopsEnvGroupVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新环境组")
    @PutMapping
    public ResponseEntity<DevopsEnvGroupVO> update(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境组信息", required = true)
            @RequestBody DevopsEnvGroupVO devopsEnvGroupVO) {
        return ResponseEntity.ok(devopsEnvGroupService.update(devopsEnvGroupVO, projectId));
    }


    /**
     * 项目下查询环境组
     *
     * @param projectId 项目id
     * @return DevopsEnvGroupVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询环境组")
    @GetMapping("/list_by_project")
    public ResponseEntity<List<DevopsEnvGroupVO>> listByProject(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsEnvGroupService.listByProject(projectId));
    }

    /**
     * 校验环境组名唯一性
     *
     * @param projectId 项目id
     * @return boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验环境组名唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境组名", required = true)
            @RequestParam String name,
            @ApiParam(value = "环境组id", required = false)
            @Encrypt
            @RequestParam(value = "group_id", required = false) Long groupId) {
        return ResponseEntity.ok(devopsEnvGroupService.checkName(name, projectId, groupId));
    }


    /**
     * 环境组删除
     *
     * @param projectId 项目id
     * @param groupId   实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "环境组删除")
    @DeleteMapping(value = "/{group_id}")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境组ID", required = true)
            @Encrypt
            @PathVariable(value = "group_id") Long groupId) {
        devopsEnvGroupService.delete(projectId, groupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 检查环境组是否存在
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "环境组存在检查")
    @GetMapping(value = "/{group_id}/check")
    public ResponseEntity<Boolean> checkExist(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境组ID", required = true)
            @PathVariable(value = "group_id") Long groupId) {
        return ResponseEntity.ok(devopsEnvGroupService.checkExist(groupId));
    }
}
