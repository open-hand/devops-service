package io.choerodon.devops.api.controller.v1;


import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsConfigMapRespVO;
import io.choerodon.devops.api.vo.DevopsConfigMapUpdateVO;
import io.choerodon.devops.api.vo.DevopsConfigMapVO;
import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/config_maps")
public class DevopsConfigMapController {


    @Autowired
    private DevopsConfigMapService devopsConfigMapService;

    /**
     * 项目下创建配置映射
     *
     * @param projectId         项目id
     * @param devopsConfigMapVO 配置映射信息
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建配置映射")
    @PostMapping
    public ResponseEntity<Void> create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody @Valid DevopsConfigMapVO devopsConfigMapVO) {
        devopsConfigMapVO.setType("create");
        devopsConfigMapService.createOrUpdate(projectId, false, devopsConfigMapVO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 项目下更新配置映射
     *
     * @param projectId               项目id
     * @param devopsConfigMapUpdateVO 配置映射信息
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下更新配置映射")
    @PutMapping
    public ResponseEntity<Void> update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody @Valid DevopsConfigMapUpdateVO devopsConfigMapUpdateVO) {
        devopsConfigMapUpdateVO.setType("update");
        devopsConfigMapService.createOrUpdate(projectId, false, ConvertUtils.convertObject(devopsConfigMapUpdateVO, DevopsConfigMapVO.class));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 配置映射删除
     *
     * @param projectId   项目id
     * @param configMapId 实例id
     * @return responseEntity
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "配置映射删除")
    @DeleteMapping(value = "/{configMap_id}")
    public ResponseEntity<Void> delete(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "实例ID", required = true)
            @PathVariable("configMap_id") Long configMapId) {
        devopsConfigMapService.delete(projectId, configMapId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 校验配置映射名唯一性
     *
     * @param projectId 项目id
     * @param name      配置映射名
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "校验配置映射名唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境ID", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "实例ID", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(devopsConfigMapService.isNameUnique(envId, name));
    }


    /**
     * 配置映射查询
     *
     * @param projectId   项目id
     * @param configMapId 配置映射Id
     * @return DevopsConfigMapRespVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询单个配置映射")
    @GetMapping("/{config_map_id}")
    public ResponseEntity<DevopsConfigMapRespVO> query(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "configMap的ID", required = true)
            @PathVariable(value = "config_map_id") Long configMapId) {
        return ResponseEntity.ok(devopsConfigMapService.query(configMapId));
    }


    /**
     * 配置映射分页查询
     *
     * @param projectId    项目id
     * @param envId        环境id
     * @param pageable     分页参数
     * @param searchParam  查询参数
     * @param appServiceId 应用id
     * @return Page of DevopsServiceVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分而查询配置映射")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<Page<DevopsConfigMapRespVO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "环境id")
            @RequestParam(value = "env_id", required = false) Long envId,
            @Encrypt
            @ApiParam(value = "应用id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageable,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return ResponseEntity.ok(devopsConfigMapService.pageByOptions(projectId, envId, pageable, searchParam, appServiceId));
    }

}
