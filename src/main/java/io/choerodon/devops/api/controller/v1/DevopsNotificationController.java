package io.choerodon.devops.api.controller.v1;

import com.github.pagehelper.PageInfo;
import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsNotificationVO;
import io.choerodon.devops.api.vo.NotificationEventVO;
import io.choerodon.devops.api.vo.NotifyEventVO;
import io.choerodon.devops.api.vo.ResourceCheckVO;
import io.choerodon.devops.app.service.DevopsNotificationService;
import io.choerodon.devops.infra.dto.DevopsNotificationDTO;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
     * @param projectId            项目id
     * @param devopsNotificationVO 通知信息
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建通知")
    @PostMapping
    public ResponseEntity<DevopsNotificationVO> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "通知信息", required = true)
            @RequestBody DevopsNotificationVO devopsNotificationVO) {
        return Optional.ofNullable(notificationService.create(projectId, devopsNotificationVO))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.notification.create"));
    }

    /**
     * 项目下更新通知
     *
     * @param projectId            项目id
     * @param devopsNotificationVO 通知信息
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新通知")
    @PutMapping
    public ResponseEntity<DevopsNotificationVO> update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "通知信息", required = true)
            @RequestBody DevopsNotificationVO devopsNotificationVO) {
        return Optional.ofNullable(notificationService.update(projectId, devopsNotificationVO))
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
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下删除通知")
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
     * 项目下获取通知详情
     *
     * @param projectId      项目id
     * @param notificationId 通知Id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下获取通知详情")
    @GetMapping(value = "/{notification_id}")
    public ResponseEntity<DevopsNotificationVO> queryById(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "通知ID", required = true)
            @PathVariable(value = "notification_id") Long notificationId) {
        return Optional.ofNullable(notificationService.queryById(notificationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.notification.query"));
    }

    /**
     * 分页查询通知列表
     *
     * @param projectId
     * @param envId
     * @param pageable
     * @param params
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "分页查询通知列表")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<PageInfo<DevopsNotificationVO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境Id", required = false)
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore Pageable pageable,
            @ApiParam(value = "查询参数")
            @RequestBody String params) {
        return Optional.ofNullable(notificationService.pageByOptions(projectId, envId, params, pageable))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.notification.list"));
    }


    /**
     * 项目下校验通知
     * 环境下每个触发事件只能有一个通知
     *
     * @param projectId 项目id
     * @param envId     通知Id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下校验通知")
    @GetMapping(value = "/check")
    public ResponseEntity<Set<String>> check(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return Optional.ofNullable(notificationService.check(projectId, envId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.notification.check"));
    }


    /**
     * 校验删除对象是否需要发送验证码
     *
     * @param projectId  项目id
     * @param envId      环境id
     * @param objectType 资源对象类型
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/check_delete_resource")
    public ResponseEntity<ResourceCheckVO> checkDeleteResource(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "资源对象类型")
            @RequestParam(value = "object_type") String objectType) {
        return Optional.ofNullable(notificationService.checkResourceDelete(envId, objectType))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.check.resource.delete"));
    }


    /**
     * 发送验证码
     *
     * @param projectId      项目Id
     * @param envId          环境Id
     * @param objectId       对象Id
     * @param notificationId 通知Id
     * @param objectType     对象类型
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/send_message")
    public void sendMessage(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "对象Id")
            @RequestParam(value = "object_id") Long objectId,
            @ApiParam(value = "通知Id")
            @RequestParam(value = "notification_id") Long notificationId,
            @ApiParam(value = "资源对象类型")
            @RequestParam(value = "object_type") String objectType) {
        notificationService.sendMessage(envId, notificationId, objectId, objectType);
    }


    /**
     * 校验验证码
     *
     * @param projectId  项目Id
     * @param envId      环境Id
     * @param objectId   对象Id
     * @param captcha    验证码
     * @param objectType 对象类型
     * @return
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/validate_captcha")
    public void validateCaptcha(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "对象Id")
            @RequestParam(value = "object_id") Long objectId,
            @ApiParam(value = "验证码")
            @RequestParam String captcha,
            @ApiParam(value = "资源对象类型")
            @RequestParam(value = "object_type") String objectType) {
        notificationService.validateCaptcha(envId, objectId, objectType, captcha);
    }
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/group_by_env")
    public ResponseEntity<NotifyEventVO> queryNotifyEventGroupByEnv(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境名")
            @RequestParam(value = "env_name",required = false) String envName) {
        return ResponseEntity.ok(notificationService.queryNotifyEventGroupByEnv(projectId, envName));
    }
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @PutMapping(value = "/batch")
    public ResponseEntity<Void> batchUpdateNotifyEvent(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody List<NotificationEventVO> notificationEventList
            ) {
        notificationService.batchUpdateNotifyEvent(projectId, notificationEventList);
        return ResponseEntity.noContent().build();
    }

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/transfer/data")
    public ResponseEntity<List<NotificationEventVO>> transferDate(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId) {
        notificationService.transferDate();
        return Optional.ofNullable(notificationService.transferDate())
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.transfer.data"));
    }



}
