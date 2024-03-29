package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
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
@RequestMapping(value = "/v1/projects/open_app_service")
public class OpenAppServiceController {
    @Autowired
    private AppServiceService applicationServiceService;

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "hand-开放平台，校验名称和code是否重复")
    @PostMapping(value = "/check_name_and_code")
    public ResponseEntity<List<CheckAppServiceCodeAndNameVO>> checkNameAndCode(
            @ApiParam(value = "项目id", required = true)
            @RequestParam(value = "project_id") Long projectId,
            @RequestBody List<CheckAppServiceCodeAndNameVO> codeAndNameVOList) {
        return ResponseEntity.ok(applicationServiceService.checkNameAndCode(projectId, codeAndNameVOList));
    }

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "hand-开放平台，创建应用服务")
    @PostMapping(value = "/create")
    public ResponseEntity<OpenAppServiceReqVO> openCreateAppService(
            @ApiParam(value = "项目id", required = true)
            @RequestParam(value = "project_id") Long projectId,
            @ApiParam(value = "服务信息", required = true)
            @RequestBody @Validated OpenAppServiceReqVO openAppServiceReqVO) {
        return ResponseEntity.ok(applicationServiceService.openCreateAppService(projectId, openAppServiceReqVO));
    }

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "hand-开放平台，获取private token")
    @GetMapping(value = "/private_token")
    public ResponseEntity<String> getPrivateToken(
            @ApiParam(value = "项目id", required = true)
            @RequestParam(value = "project_id") Long projectId,
            @ApiParam(value = "服务编码", required = true)
            @RequestParam(value = "serviceCode") String serviceCode,
            @ApiParam(value = "用户邮箱", required = true)
            @RequestParam(value = "email") String email) {
        return Results.success(applicationServiceService.getPrivateToken(projectId, serviceCode, email));
    }

    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "hand-开放平台，获取ssh地址")
    @GetMapping(value = "/ssh_url")
    public String getSshUrl(
            @ApiParam(value = "项目id", required = true)
            @RequestParam(value = "project_id") Long projectId,
            @ApiParam(value = "组织编码", required = true)
            @RequestParam(value = "orgCode") String orgCode,
            @ApiParam(value = "项目编码", required = true)
            @RequestParam(value = "projectCode") String projectCode,
            @ApiParam(value = "服务编码", required = true)
            @RequestParam(value = "serviceCode") String serviceCode) {
        return applicationServiceService.getSshUrl(projectId, orgCode, projectCode, serviceCode);
    }

}
