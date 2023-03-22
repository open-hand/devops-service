package io.choerodon.devops.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsDeployValueUpdateVO;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.app.service.DevopsDeployValueService;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

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
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用Id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @Encrypt
            @ApiParam(value = "环境Id")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "lastUpdateDate", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String params) {
        return ResponseEntity.ok(devopsDeployValueService.pageByOptions(projectId, appServiceId, envId, pageable, params));
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
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署配置相关信息")
            @RequestBody @Valid DevopsDeployValueVO devopsDeployValueVO) {
        return ResponseEntity.ok(devopsDeployValueService.createOrUpdate(projectId, devopsDeployValueVO));
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
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署配置相关信息")
            @RequestBody @Valid DevopsDeployValueUpdateVO devopsDeployValueUpdateVO) {
        return ResponseEntity.ok((devopsDeployValueService.createOrUpdate(projectId, ConvertUtils.convertObject(devopsDeployValueUpdateVO, DevopsDeployValueVO.class))));
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
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "配置Id", required = true)
            @RequestParam(value = "value_id") Long valueId) {
        return ResponseEntity.ok(devopsDeployValueService.query(projectId, valueId));
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
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "名称", required = true)
            @RequestParam(value = "name") String name,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return ResponseEntity.ok(devopsDeployValueService.isNameUnique(projectId, name, envId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "检测能否删除")
    @GetMapping("/check_delete")
    public ResponseEntity<List<PipelineInstanceReferenceVO>> checkDelete(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "valueId", required = true)
            @RequestParam(value = "value_id") Long valueId) {
        return ResponseEntity.ok(devopsDeployValueService.checkDelete(projectId, valueId));
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
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用服务Id", required = true)
            @RequestParam(value = "app_service_id") Long appServiceId,
            @Encrypt
            @ApiParam(value = "环境Id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "部署配置名", required = false)
            @RequestParam(value = "name", required = false) String name
            ) {
        return ResponseEntity.ok(devopsDeployValueService.listByEnvAndApp(projectId, appServiceId, envId, name));
    }

    @ApiOperation(value = "根据实例id查询管理的部署配置列表")
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @GetMapping("/list_value_by_instance_id")
    public ResponseEntity<List<DevopsDeployValueVO>> listValueByInstanceId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @Encrypt
            @RequestParam(value = "instance_id") Long instanceId) {
        return Results.success(devopsDeployValueService.listValueByInstanceId(projectId, instanceId));
    }
}
