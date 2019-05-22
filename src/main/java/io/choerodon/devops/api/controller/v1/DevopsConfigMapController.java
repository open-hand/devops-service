package io.choerodon.devops.api.controller.v1;


import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.DevopsConfigMapDTO;
import io.choerodon.devops.api.dto.DevopsConfigMapRepDTO;
import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/config_maps")
public class DevopsConfigMapController {


    @Autowired
    private DevopsConfigMapService devopsConfigMapService;

    /**
     * 项目下创建配置映射
     *
     * @param projectId          项目id
     * @param devopsConfigMapDTO 配置映射信息
     * @return ResponseEntity
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建配置映射")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @RequestBody DevopsConfigMapDTO devopsConfigMapDTO) {
        devopsConfigMapService.createOrUpdate(projectId, false, devopsConfigMapDTO);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 配置映射删除
     *
     * @param projectId   项目id
     * @param configMapId 实例id
     * @return responseEntity
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "配置映射删除")
    @DeleteMapping(value = "/{configMap_id}/delete")
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
     * @param projectId     项目id
     * @param configMapName 配置映射名
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "校验配置映射名唯一性")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "项目 ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "实例ID", required = true)
            @RequestParam String configMapName) {
        devopsConfigMapService.checkName(envId, configMapName);
    }


    /**
     * 配置映射查询
     *
     * @param projectId   项目id
     * @param configMapId 配置映射Id
     * @return DevopsConfigMapRepDTO
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "项目下创建配置映射")
    @GetMapping("/{configMap_id}")
    public ResponseEntity<DevopsConfigMapRepDTO> query(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "域名信息", required = true)
            @PathVariable(value = "configMap_id") Long configMapId) {
        return Optional.ofNullable(devopsConfigMapService.query(configMapId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.configMap.query"));
    }


    /**
     * 配置映射分页查询
     *
     * @param projectId   项目id
     * @param envId       环境id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return Page of DevopsServiceDTO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "环境配置映射查询")
    @CustomPageRequest
    @PostMapping(value = "/{envId}/listByEnv")
    public ResponseEntity<Page<DevopsConfigMapRepDTO>> listByEnv(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "envId") Long envId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsConfigMapService.listByEnv(projectId, envId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.configMap.query"));
    }

}
