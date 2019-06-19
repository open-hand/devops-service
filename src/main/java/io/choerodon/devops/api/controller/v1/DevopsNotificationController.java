package io.choerodon.devops.api.controller.v1;

import java.util.Optional;
import java.util.Set;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.DevopsNotificationDTO;
import io.choerodon.devops.api.dto.ResourceCheckDTO;
import io.choerodon.devops.app.service.DevopsNotificationService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建通知")
    @PostMapping
    public ResponseEntity<DevopsNotificationDTO> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "通知信息", required = true)
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
    @Permission(type= ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下更新通知")
    @PutMapping
    public ResponseEntity<DevopsNotificationDTO> update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "通知信息", required = true)
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
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
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
     * 项目下通知详情
     *
     * @param projectId      项目id
     * @param notificationId 通知Id
     * @return ResponseEntity
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下获取通知详情")
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
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "通知列表")
    @CustomPageRequest
    @PostMapping(value = "/list")
    public ResponseEntity<PageInfo<DevopsNotificationDTO>> listByOptions(
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
     * @param projectId 项目id
     * @param envId     通知Id
     * @return ResponseEntity
     */
    @Permission(type= ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER})
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
     * @param envId  环境id
     * @param objectType  资源对象类型
     * @return
     */
    @Permission(type= ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/check_delete_resource")
    public ResponseEntity<ResourceCheckDTO> checkDeleteResource(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "资源对象类型")
            @RequestParam String objectType) {
        return Optional.ofNullable(notificationService.checkResourceDelete(envId, objectType))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.check.resource.delete"));
    }


    /**
     * 发送验证码
     *
     * @param projectId  项目Id
     * @param envId   环境Id
     * @param objectId  对象Id
     * @param notificationId  通知Id
     * @param objectType  对象类型
     * @return
     */
    @Permission(type= ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
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
            @RequestParam String objectType) {
        notificationService.sendMessage(envId, notificationId, objectId, objectType);
    }



    /**
     * 校验验证码
     *
     * @param projectId  项目Id
     * @param envId   环境Id
     * @param objectId  对象Id
     * @param captcha  验证码
     * @param objectType  对象类型
     * @return
     */
    @Permission(type= ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
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
            @RequestParam String objectType) {
        notificationService.validateCaptcha(envId, objectId,objectType,captcha);
    }
}
