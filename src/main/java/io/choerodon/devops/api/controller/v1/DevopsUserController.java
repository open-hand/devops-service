package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/users")
public class DevopsUserController {


    @Autowired
    private UserAttrService userAttrService;

    /**
     * 根据用户Id查询gitlab用户Id
     *
     * @return UserAttrDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据用户Id查询gitlab用户Id")
    @GetMapping("/{user_id}")
    public ResponseEntity<UserAttrVO> queryByUserId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "用户id", required = true)
            @PathVariable(value = "user_id") Long userId) {
        return ResponseEntity.ok(userAttrService.queryByUserId(userId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询有应用服务权限的用户列表")
    @CustomPageRequest
    @PostMapping("/app_services/{app_service_id}")
    public ResponseEntity<Page<IamUserDTO>> queryByAppServiceId(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "用户id", required = true)
            @PathVariable(value = "app_service_id") Long appServiceId,
            PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params
    ) {
        return ResponseEntity.ok(userAttrService.queryByAppServiceId(projectId, appServiceId, pageRequest, params));
    }
}
