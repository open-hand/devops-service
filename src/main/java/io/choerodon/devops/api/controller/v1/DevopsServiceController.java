package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.DevopsServiceDTO;
import io.choerodon.devops.api.dto.DevopsServiceReqDTO;
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

    private DevopsServiceService devopsServiceService;

    public DevopsServiceController(DevopsServiceService devopsServiceService) {
        this.devopsServiceService = devopsServiceService;
    }

    /**
     * 检查网络唯一性
     *
     * @param projectId 项目ID
     * @param envId     环境ID
     * @param name      网络名
     * @return Boolean
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "检查网络唯一性")
    @GetMapping(value = "/check")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam Long envId,
            @ApiParam(value = "网络名", required = true)
            @RequestParam String name) {
        return Optional.ofNullable(devopsServiceService.checkName(projectId, envId, name))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.service.name.check"));
    }

    /**
     * 部署网络
     *
     * @param projectId           项目id
     * @param devopsServiceReqDTO 部署网络参数
     * @return Boolean
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "部署网络")
    @PostMapping
    public ResponseEntity<Boolean> create(@ApiParam(value = "项目ID", required = true)
                                          @PathVariable(value = "project_id") Long projectId,
                                          @ApiParam(value = "部署网络参数", required = true)
                                          @RequestBody @Valid DevopsServiceReqDTO devopsServiceReqDTO) {
        return Optional.ofNullable(
                devopsServiceService.insertDevopsService(projectId, devopsServiceReqDTO, false))
                .map(target -> new ResponseEntity<>(target, HttpStatus.CREATED))
                .orElseThrow(() -> new CommonException("error.service.deploy"));
    }

    /**
     * 更新网络
     *
     * @param projectId           项目id
     * @param id                  网络ID
     * @param devopsServiceReqDTO 部署网络参数
     * @return Boolean
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "更新网络")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Boolean> update(@ApiParam(value = "项目ID", required = true)
                                          @PathVariable(value = "project_id") Long projectId,
                                          @ApiParam(value = "网络ID", required = true)
                                          @PathVariable Long id,
                                          @ApiParam(value = "部署网络参数", required = true)
                                          @RequestBody DevopsServiceReqDTO devopsServiceReqDTO) {
        return Optional.ofNullable(
                devopsServiceService.updateDevopsService(projectId, id, devopsServiceReqDTO, false))
                .map(target -> new ResponseEntity<>(target, HttpStatus.CREATED))
                .orElseThrow(() -> new CommonException("error.app.k8s.service.update"));
    }

    /**
     * 删除网络
     *
     * @param projectId 项目id
     * @param id        网络ID
     * @return ResponseEntity
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "删除网络")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@ApiParam(value = "项目ID", required = true)
                                 @PathVariable(value = "project_id") Long projectId,
                                 @ApiParam(value = "网络ID", required = true)
                                 @PathVariable Long id) {
        devopsServiceService.deleteDevopsService(id, false);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 分页查询网络列表
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return Page of DevopsServiceDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "分页查询网络列表")
    @CustomPageRequest
    @PostMapping(value = "/list_by_options")
    public ResponseEntity<Page<DevopsServiceDTO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsServiceService.listDevopsServiceByPage(projectId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.CREATED))
                .orElseThrow(() -> new CommonException("error.app.k8s.service.query"));
    }

    /**
     * 分页查询网络列表
     *
     * @param projectId 项目id
     * @param envId     参数
     * @return List of DevopsServiceDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "根据环境查询网络列表")
    @GetMapping
    public ResponseEntity<List<DevopsServiceDTO>> listByEnvId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam Long envId) {
        return Optional.ofNullable(devopsServiceService.listDevopsService(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.CREATED))
                .orElseThrow(() -> new CommonException("error.app.k8s.service.env.query"));
    }

    /**
     * 查询单个网络
     *
     * @param projectId 项目id
     * @param id        网络id
     * @return DevopsServiceDTO
     */
    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "查询单个网络")
    @GetMapping(value = "/{id}")
    public ResponseEntity<DevopsServiceDTO> query(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "网络ID", required = true)
            @PathVariable Long id) {
        return Optional.ofNullable(devopsServiceService.query(id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.k8s.service.query"));
    }


    /**
     * 环境总览网络查询
     *
     * @param projectId   项目id
     * @param envId       环境id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return Page of DevopsServiceDTO
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER,
                    InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "环境总览网络查询")
    @CustomPageRequest
    @PostMapping(value = "/{envId}/listByEnv")
    public ResponseEntity<Page<DevopsServiceDTO>> listByEnv(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "envId") Long envId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsServiceService.listByEnv(projectId, envId, pageRequest, searchParam))
                .map(target -> new ResponseEntity<>(target, HttpStatus.CREATED))
                .orElseThrow(() -> new CommonException("error.app.k8s.service.query"));
    }
}