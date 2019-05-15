package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

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

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.DevopsNotificationDTO;
import io.choerodon.devops.app.service.DevopsNotificationService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:40 2019/5/13
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/notification")
public class DevopsNotificationController {
    @Autowired
    private DevopsNotificationService notificationService;

    /**
     * 项目下创建通知
     *
     * @param projectId       项目id
     * @param notificationDTO 通知信息
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建域名")
    @PostMapping
    public ResponseEntity<DevopsNotificationDTO> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody DevopsNotificationDTO notificationDTO) {
        return Optional.ofNullable(notificationService.create(projectId, notificationDTO))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.notification.create"));
    }

    /**
     * 项目下更新通知
     *
     * @param projectId       项目id
     * @param notificationDTO 通知信息
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建域名")
    @PutMapping
    public ResponseEntity<DevopsNotificationDTO> update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody DevopsNotificationDTO notificationDTO) {
        return Optional.ofNullable(notificationService.update(projectId, notificationDTO))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.notification.update"));
    }


    /**
     * 项目下删除通知
     *
     * @param projectId      项目id
     * @param notificationId 通知Id
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建域名")
    @DeleteMapping(value = "/{notification_id}")
    public ResponseEntity delete(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "通知ID", required = true)
            @PathVariable(value = "notification_id") Long notificationId) {
        notificationService.delete(notificationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下通知详情
     *
     * @param projectId      项目id
     * @param notificationId 通知Id
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建域名")
    @GetMapping(value = "/{notification_id}")
    public ResponseEntity<DevopsNotificationDTO> queryById(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "通知ID", required = true)
            @PathVariable(value = "notification_id") Long notificationId) {
        return Optional.ofNullable(notificationService.queryById(notificationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.notification.query"));
    }

    /**
     * 通知列表
     *
     * @param projectId
     * @param envId
     * @param pageRequest
     * @param params
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建域名")
    @CustomPageRequest
    @PostMapping(value = "/list")
    public ResponseEntity<Page<DevopsNotificationDTO>> listByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境Id", required = false)
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody String params) {
        return Optional.ofNullable(notificationService.listByOptions(projectId, envId, params, pageRequest))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.notification.list"));
    }

    /**
     * 项目下校验通知
     * 环境下每个触发事件只能有一个通知
     *
     * @param projectId          项目id
     * @param envId              通知Id
     * @param notifyTriggerEvent
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建域名")
    @PostMapping(value = "/check")
    public ResponseEntity<Boolean> check(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "触发事件", required = true)
            @RequestBody List<String> notifyTriggerEvent) {
        return Optional.ofNullable(notificationService.check(projectId, envId, notifyTriggerEvent))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.notification.check"));
    }

}
