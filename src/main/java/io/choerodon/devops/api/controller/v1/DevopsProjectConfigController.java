package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.DefaultConfigVO;
import io.choerodon.devops.api.vo.DevopsConfigRepVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/project_config")
public class DevopsProjectConfigController {

    @Autowired
    DevopsConfigService devopsConfigService;

    @Autowired
    AppServiceService appServiceService;

    /**
     * 项目下处理配置 （项目下配置库的接口不要了）
     *
     * @param projectId         项目id
     * @param devopsConfigRepVO 配置信息
     * @return void
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下创建配置")
    @PostMapping
    public ResponseEntity<Void> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "配置信息", required = true)
            @RequestBody DevopsConfigRepVO devopsConfigRepVO) {
        devopsConfigService.operateConfig(projectId, ResourceLevel.PROJECT.value(), devopsConfigRepVO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 项目下查询配置详情
     * *
     *
     * @param projectId 项目id
     * @return 配置详情
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "项目下查询配置详情")
    @GetMapping
    public ResponseEntity<DevopsConfigRepVO> query(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(
                devopsConfigService.queryConfig(projectId, ResourceLevel.PROJECT.value()))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.get.type"));
    }


    /**
     * 获取项目默认的配置
     *
     * @param projectId 项目id
     * @return ProjectDefaultConfigDTO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "获取项目默认的配置")
    @GetMapping("/default_config")
    public ResponseEntity<DefaultConfigVO> queryProjectDefaultConfig(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return Optional.ofNullable(
                devopsConfigService.queryDefaultConfig(projectId, ResourceLevel.PROJECT.value()))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.project.config.get"));
    }


    /**
     * 校验harbor配置信息是否正确
     *
     * @param url      harbor地址
     * @param userName harbor用户名
     * @param password harbor密码
     * @param project  harbor项目
     * @param email    harbor邮箱
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验harbor配置信息是否正确")
    @GetMapping(value = "/check_harbor")
    public void checkHarbor(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "harbor地址", required = true)
            @RequestParam String url,
            @ApiParam(value = "harbor用户名", required = true)
            @RequestParam String userName,
            @ApiParam(value = "harbor密码", required = true)
            @RequestParam String password,
            @ApiParam(value = "harborProject")
            @RequestParam(required = false) String project,
            @ApiParam(value = "harbor邮箱", required = true)
            @RequestParam String email) {
        appServiceService.checkHarbor(url, userName, password, project, email);
    }


    /**
     * 校验chart配置信息是否正确
     *
     * @param configVO chartMuseum信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验chart配置信息是否正确")
    @PostMapping(value = "/check_chart")
    public ResponseEntity<Boolean> checkChart(
            @ApiParam(value = "项目id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "chartMuseum信息", required = true)
            @RequestBody ConfigVO configVO) {
        return Optional.ofNullable(
                appServiceService.checkChart(configVO.getUrl(), configVO.getUserName(), configVO.getPassword()))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.connection.failed"));
    }
}
