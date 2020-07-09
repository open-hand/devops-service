package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsDeployValueUpdateVO;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.app.service.DevopsDeployValueService;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:56 2019/4/10
 * Description:
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/deploy_value")
public class DevopsDeployValueController {
    @Autowired
    private DevopsDeployValueService devopsDeployValueService;

    /**
     * 项目下获取部署配置
     *
     * @param projectId 项目Id
     * @param pageable  分页参数
     * @param params    查询参数
     * @return 部署配置
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下分页查询部署配置")
    @CustomPageRequest
    @PostMapping("/page_by_options")
    public ResponseEntity<Page<DevopsDeployValueVO>> pageByOptions(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @Encrypt
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return Optional.ofNullable(devopsDeployValueService.pageByOptions(projectId, appServiceId, envId, pageable, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.list"));
    }

    /**
     * 项目下创建部署配置
     *
     * @param projectId           项目Id
     * @param devopsDeployValueVO 配置信息
     * @return 部署配置
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建部署配置")
    @PostMapping
    public ResponseEntity<DevopsDeployValueVO> create(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署配置相关信息")
            @RequestBody @Valid DevopsDeployValueVO devopsDeployValueVO) {
        return Optional.ofNullable(devopsDeployValueService.createOrUpdate(projectId, devopsDeployValueVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.create"));
    }

    /**
     * 项目下更新部署配置
     *
     * @param projectId                 项目Id
     * @param devopsDeployValueUpdateVO 配置信息
     * @return 部署配置
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下更新部署配置")
    @PutMapping
    public ResponseEntity<DevopsDeployValueVO> update(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署配置相关信息")
            @RequestBody @Valid DevopsDeployValueUpdateVO devopsDeployValueUpdateVO) {
        return Optional.ofNullable(devopsDeployValueService.createOrUpdate(projectId, ConvertUtils.convertObject(devopsDeployValueUpdateVO, DevopsDeployValueVO.class)))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.update"));
    }

    /**
     * 项目下查询配置详情
     *
     * @param projectId 项目Id
     * @param valueId   配置Id
     * @return 配置信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下查询配置详情")
    @GetMapping
    public ResponseEntity<DevopsDeployValueVO> query(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "配置Id", required = true)
            @RequestParam(value = "value_id") Long valueId) {
        return Optional.ofNullable(devopsDeployValueService.query(projectId, valueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.queryById"));
    }

    /**
     * 项目下删除配置
     *
     * @param projectId 项目Id
     * @param valueId   配置Id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下删除配置")
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "配置id", required = true)
            @RequestParam(value = "value_id") Long valueId) {
        devopsDeployValueService.delete(projectId, valueId);
        return ResponseEntity.noContent().build();
    }


    /**
     * 校验部署配置的名称在环境下唯一
     *
     * @param projectId 项目id
     * @param name      名称
     * @param envId     环境id
     * @return 没有内容则名称合法
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "校验部署配置的名称在环境下唯一")
    @GetMapping("/check_name")
    public ResponseEntity<Boolean> checkName(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "名称", required = true)
            @RequestParam(value = "name") String name,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return ResponseEntity.ok(devopsDeployValueService.isNameUnique(projectId, name, envId));
    }

    /**
     * 检测能否删除
     *
     * @param projectId 项目id
     * @param valueId   配置id
     * @return true则可以删除
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检测能否删除")
    @GetMapping("/check_delete")
    public ResponseEntity<Boolean> checkDelete(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "valueId", required = true)
            @RequestParam(value = "value_id") Long valueId) {
        return Optional.ofNullable(devopsDeployValueService.checkDelete(projectId, valueId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.check.delete"));
    }

    /**
     * 根据应用服务Id和环境Id获取配置
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @param envId        环境id
     * @return 配置信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据应用服务Id和环境Id获取配置")
    @GetMapping("/list_by_env_and_app")
    public ResponseEntity<List<DevopsDeployValueVO>> listByEnvAndApp(
            @Encrypt
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务Id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @Encrypt
            @ApiParam(value = "环境Id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsDeployValueService.listByEnvAndApp(projectId, appServiceId, envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.pipeline.value.queryByIds"));
    }
}
