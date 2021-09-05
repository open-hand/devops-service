package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.devops.api.vo.CheckAppServiceCodeAndNameVO;
import io.choerodon.devops.api.vo.open.OpenAppServiceReqVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @Author: scp
 * @Description: 开放平台应用服务需要接口
 * @Date: Created in 2021/9/3
 * @Modified By:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/open_app_service")
public class OpenAppServiceController {
    @Autowired
    private AppServiceService applicationServiceService;

    @Permission(permissionLogin = true)
    @ApiOperation(value = "hand-开放平台，校验名称和code是否重复")
    @PostMapping(value = "/check_name_and_code")
    public ResponseEntity<List<CheckAppServiceCodeAndNameVO>> checkNameAndCode(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @RequestBody List<CheckAppServiceCodeAndNameVO> codeAndNameVOList) {
        return ResponseEntity.ok(applicationServiceService.checkNameAndCode(projectId, codeAndNameVOList));
    }

    @Permission(permissionLogin = true)
    @ApiOperation(value = "hand-开放平台，创建应用服务")
    @PostMapping(value = "/create")
    public ResponseEntity<OpenAppServiceReqVO> openCreateAppService(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务信息", required = true)
            @RequestBody @Validated OpenAppServiceReqVO openAppServiceReqVO) {
        return ResponseEntity.ok(applicationServiceService.openCreateAppService(projectId, openAppServiceReqVO));
    }

    @Permission(permissionLogin = true)
    @ApiOperation(value = "hand-开放平台，获取private token")
    @GetMapping(value = "/private_token")
    public String getPrivateToken(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "服务编码", required = true)
            @RequestParam(value = "serviceCode") String serviceCode,
            @ApiParam(value = "用户邮箱", required = true)
            @RequestParam(value = "email") String email,
            @ApiParam(value = "项目id", required = true)
            @RequestParam(value = "gitlab_project_id") Long gitlabProjectId) {
        return applicationServiceService.getPrivateToken(projectId, serviceCode, email, gitlabProjectId);
    }
}
