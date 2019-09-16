package io.choerodon.devops.api.controller.v1;


import java.util.Optional;

import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsConfigMapRespVO;
import io.choerodon.devops.api.vo.DevopsConfigMapUpdateVO;
import io.choerodon.devops.api.vo.DevopsConfigMapVO;
import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;

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
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建配置映射")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @Valid @RequestBody DevopsConfigMapVO devopsConfigMapVO) {
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
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下更新配置映射")
    @PutMapping
    public ResponseEntity update(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @Valid @RequestBody DevopsConfigMapUpdateVO devopsConfigMapUpdateVO) {
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
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "配置映射删除")
    @DeleteMapping(value = "/{configMap_id}")
    public ResponseEntity delete(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable("configMap_id") Long configMapId) {
        devopsConfigMapService.delete(configMapId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 校验配置映射名唯一性
     *
     * @param projectId 项目id
     * @param name      配置映射名
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "校验配置映射名唯一性")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "实例ID", required = true)
            @RequestParam String name) {
        devopsConfigMapService.checkName(envId, name);
    }


    /**
     * 配置映射查询
     *
     * @param projectId   项目id
     * @param configMapId 配置映射Id
     * @return DevopsConfigMapRespVO
     */
    @Permission(type = ResourceType.PROJECT, roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "配置映射查询")
    @GetMapping("/{config_map_id}")
    public ResponseEntity<DevopsConfigMapRespVO> query(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "configMap的ID", required = true)
            @PathVariable(value = "config_map_id") Long configMapId) {
        return Optional.ofNullable(devopsConfigMapService.query(configMapId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.configMap.query"));
    }


    /**
     * 配置映射分页查询
     *
     * @param projectId    项目id
     * @param envId        环境id
     * @param pageRequest  分页参数
     * @param searchParam  查询参数
     * @param appServiceId 应用id
     * @return Page of DevopsServiceVO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "环境配置映射查询")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<PageInfo<DevopsConfigMapRespVO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id")
            @RequestParam(value = "env_id", required = false) Long envId,
            @ApiParam(value = "应用id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsConfigMapService.pageByOptions(projectId, envId, pageRequest, searchParam, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.configMap.query"));
    }

}
