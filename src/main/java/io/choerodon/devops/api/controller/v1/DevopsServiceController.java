package io.choerodon.devops.api.controller.v1;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.api.vo.DevopsServiceVO;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Zenger on 2018/4/13.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/service")
public class DevopsServiceController {

    @Autowired
    private DevopsServiceService devopsServiceService;

    /**
     * 检查网络唯一性
     *
     * @param projectId 项目ID
     * @param envId     环境ID
     * @param name      网络名
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查网络名称唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "网络名", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(devopsServiceService.checkName(envId, name));
    }

    /**
     * 部署网络
     *
     * @param projectId          项目id
     * @param devopsServiceReqVO 部署网络参数
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "部署网络")
    @PostMapping
    public ResponseEntity<Boolean> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "部署网络参数", required = true)
            @RequestBody @Valid DevopsServiceReqVO devopsServiceReqVO) {
        return ResponseEntity.ok(devopsServiceService.create(projectId, devopsServiceReqVO));
    }

    /**
     * 更新网络
     *
     * @param projectId          项目id
     * @param id                 网络ID
     * @param devopsServiceReqVO 部署网络参数
     * @return Boolean
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "更新网络")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Boolean> update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "网络ID", required = true)
            @PathVariable Long id,
            @ApiParam(value = "部署网络参数", required = true)
            @RequestBody DevopsServiceReqVO devopsServiceReqVO) {
        return ResponseEntity.ok(devopsServiceService.update(projectId, id, devopsServiceReqVO));
    }

    /**
     * 删除网络
     *
     * @param projectId 项目id
     * @param id        网络ID
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除网络")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "网络ID", required = true)
            @PathVariable Long id) {
        devopsServiceService.delete(projectId, id);
        return ResponseEntity.noContent().build();
    }


    /**
     * 根据环境查询网络列表
     *
     * @param projectId 项目id
     * @param envId     参数
     * @return List of DevopsServiceVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据环境查询网络列表")
    @GetMapping("/list_by_env")
    public ResponseEntity<List<DevopsServiceVO>> listByEnvId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "服务id", required = false)
            @RequestParam(value = "app_service_id", required = false) Long appServiceId) {
        return ResponseEntity.ok(devopsServiceService.listByEnvIdAndAppServiceId(envId, appServiceId));
    }

    /**
     * 查询单个网络
     *
     * @param projectId 项目id
     * @param id        网络id
     * @return DevopsServiceVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询单个网络")
    @GetMapping(value = "/{id}")
    public ResponseEntity<DevopsServiceVO> query(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "网络ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(devopsServiceService.querySingleService(id));
    }


    /**
     * 根据网络名查询网络
     *
     * @param projectId 项目id
     * @param envId     网络id
     * @param name      网络名
     * @return DevopsServiceVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据网络名查询网络")
    @GetMapping(value = "/query_by_name")
    public ResponseEntity<DevopsServiceVO> queryByName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境Id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "网络名", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(devopsServiceService.queryByName(envId, name));
    }

    /**
     * 环境总览分页查询网络
     *
     * @param projectId   项目id
     * @param envId       环境id
     * @param pageable    分页参数
     * @param searchParam 查询参数
     * @return Page of DevopsServiceVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询网络")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<Page<DevopsServiceVO>> pageByEnv(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "服务id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return ResponseEntity.ok(devopsServiceService.pageByEnv(projectId, envId, pageable, searchParam, appServiceId));
    }


    /**
     * 查询实例下关联的网络域名（不包含chart）
     *
     * @param projectId  项目id
     * @param instanceId 实例Id
     * @param pageable   分页参数
     * @return Page of DevopsServiceVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询实例下关联的网络域名（不包含chart）")
    @CustomPageRequest
    @PostMapping(value = "/page_by_instance")
    public ResponseEntity<Page<DevopsServiceVO>> pageByInstance(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id")
            @RequestParam(value = "env_id") Long envId,
            @Encrypt
            @ApiParam(value = "实例id")
            @RequestParam(value = "instance_id", required = false) Long instanceId,
            @Encrypt
            @ApiParam(value = "服务id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return ResponseEntity.ok(devopsServiceService.pageByInstance(projectId, envId, instanceId, pageable, appServiceId, searchParam));
    }
}