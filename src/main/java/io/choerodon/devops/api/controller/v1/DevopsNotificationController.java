package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsNotificationTransferDataVO;
import io.choerodon.devops.api.vo.ResourceCheckVO;
import io.choerodon.devops.app.service.DevopsNotificationService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @GetMapping(value = "/transfer/data")
    public ResponseEntity<List<DevopsNotificationTransferDataVO>> transferDate(
            @ApiParam(value = "项目ID")
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(notificationService.transferDate())
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.transfer.data"));
    }



}
