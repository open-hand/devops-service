package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ResourceCheckVO;
import io.choerodon.devops.app.service.DevopsNotificationService;
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
     * 校验删除对象是否需要发送验证码
     *
     * @param projectId  项目id
     * @param envId      环境id
     * @param objectType 资源对象类型
     * @return 校验结果
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/check_delete_resource")
    @ApiOperation(value = "校验删除对象是否需要发送验证码")
    public ResponseEntity<ResourceCheckVO> checkDeleteResource(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "资源对象类型")
            @RequestParam(value = "object_type") String objectType) {
        return ResponseEntity.ok(notificationService.checkResourceDelete(projectId, envId, objectType));
    }


    /**
     * 发送验证码
     *
     * @param projectId      项目Id
     * @param envId          环境Id
     * @param objectId       对象Id
     * @param notificationId 通知Id
     * @param objectType     对象类型
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/send_message")
    @ApiOperation(value = "发送验证码")
    public void sendMessage(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "对象Id")
            @RequestParam(value = "object_id") Long objectId,
            @Encrypt
            @ApiParam(value = "通知Id")
            @RequestParam(value = "notification_id") Long notificationId,
            @ApiParam(value = "资源对象类型")
            @RequestParam(value = "object_type") String objectType) {
        notificationService.sendMessage(projectId, envId, notificationId, objectId, objectType);
    }


    /**
     * 校验验证码
     *
     * @param projectId  项目Id
     * @param envId      环境Id
     * @param objectId   对象Id
     * @param captcha    验证码
     * @param objectType 对象类型
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/validate_captcha")
    @ApiOperation(value = "校验验证码")
    public void validateCaptcha(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "对象Id")
            @RequestParam(value = "object_id") Long objectId,
            @ApiParam(value = "验证码")
            @RequestParam String captcha,
            @ApiParam(value = "资源对象类型")
            @RequestParam(value = "object_type") String objectType) {
        notificationService.validateCaptcha(projectId, envId, objectId, objectType, captcha);
    }
}
