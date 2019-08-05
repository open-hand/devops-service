package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.annotation.Permission;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.vo.DevopsServiceVO;
import io.choerodon.devops.api.vo.DevopsServiceReqVO;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.mybatis.annotation.SortDefault;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by Zenger on 2018/4/13.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/service")
public class DevopsServiceController {

    public static final String ERROR_APP_K8S_SERVICE_QUERY = "error.app.k8s.service.query";

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
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "检查网络唯一性")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "网络名", required = true)
            @RequestParam String name) {
        return Optional.ofNullable(devopsServiceService.checkName(envId, name))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.service.name.check"));
    }

    /**
     * 部署网络
     *
     * @param projectId           项目id
     * @param devopsServiceReqVO 部署网络参数
     * @return Boolean
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "部署网络")
    @PostMapping
    public ResponseEntity<Boolean> create(@ApiParam(value = "项目ID", required = true)
                                          @PathVariable(value = "project_id") Long projectId,
                                          @ApiParam(value = "部署网络参数", required = true)
                                          @RequestBody @Valid DevopsServiceReqVO devopsServiceReqVO) {
        return Optional.ofNullable(
                devopsServiceService.create(projectId, devopsServiceReqVO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.CREATED))
                .orElseThrow(() -> new CommonException("error.service.deploy"));
    }

    /**
     * 更新网络
     *
     * @param projectId           项目id
     * @param id                  网络ID
     * @param devopsServiceReqVO 部署网络参数
     * @return Boolean
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "更新网络")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Boolean> update(@ApiParam(value = "项目ID", required = true)
                                          @PathVariable(value = "project_id") Long projectId,
                                          @ApiParam(value = "网络ID", required = true)
                                          @PathVariable Long id,
                                          @ApiParam(value = "部署网络参数", required = true)
                                          @RequestBody DevopsServiceReqVO devopsServiceReqVO) {
        return Optional.ofNullable(
                devopsServiceService.update(projectId, id, devopsServiceReqVO))
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
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除网络")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@ApiParam(value = "项目ID", required = true)
                                 @PathVariable(value = "project_id") Long projectId,
                                 @ApiParam(value = "网络ID", required = true)
                                 @PathVariable Long id) {
        devopsServiceService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    /**
     * 根据环境查询网络列表
     *
     * @param projectId 项目id
     * @param envId     参数
     * @return List of DevopsServiceVO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据环境查询网络列表")
    @GetMapping("/list_by_env")
    public ResponseEntity<List<DevopsServiceVO>> listByEnvId(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境ID", required = true)
            @RequestParam(value = "env_id") Long envId) {
        return Optional.ofNullable(devopsServiceService.listByEnvId(envId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.app.k8s.service.env.query"));
    }

    /**
     * 查询单个网络
     *
     * @param projectId 项目id
     * @param id        网络id
     * @return DevopsServiceVO
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询单个网络")
    @GetMapping(value = "/{id}")
    public ResponseEntity<DevopsServiceVO> query(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "网络ID", required = true)
            @PathVariable Long id) {
        return Optional.ofNullable(devopsServiceService.query(id))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APP_K8S_SERVICE_QUERY));
    }


    /**
     * 根据网络名查询网络
     *
     * @param projectId 项目id
     * @param envId   网络id
     * @param name  网络名
     * @return DevopsServiceVO
     */
    @Permission(type= ResourceType.PROJECT,roles = {InitRoleCode.PROJECT_OWNER,
            InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "根据网络名查询网络")
    @GetMapping(value = "/query_by_name")
    public ResponseEntity<DevopsServiceVO> queryByName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境Id", required = true)
            @RequestParam(value = "env_id") Long envId,
            @ApiParam(value = "网络名", required = true)
            @RequestParam String name) {
        return Optional.ofNullable(devopsServiceService.queryByName(envId,name))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APP_K8S_SERVICE_QUERY));
    }

    /**
     * 环境总览分页查询网络
     *
     * @param projectId   项目id
     * @param envId       环境id
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return Page of DevopsServiceVO
     */
    @Permission(type = ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "环境总览分页查询网络")
    @CustomPageRequest
    @PostMapping(value = "/{env_id}/page_by_env")
    public ResponseEntity<PageInfo<DevopsServiceVO>> pageByEnv(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "环境id", required = true)
            @PathVariable(value = "env_id") Long envId,
            @ApiParam(value = "服务id")
            @RequestParam(value = "app_service_id",required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return Optional.ofNullable(devopsServiceService.pageByEnv(projectId, envId, pageRequest, searchParam, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APP_K8S_SERVICE_QUERY));
    }


    /**
     * 查询实例下关联的网络域名（不包含chart）
     *
     * @param projectId   项目id
     * @param instanceId   实例Id
     * @param pageRequest 分页参数
     * @return Page of DevopsServiceVO
     */
    @Permission(type= ResourceType.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "查询实例下关联的网络域名（不包含chart）")
    @CustomPageRequest
    @PostMapping(value = "/page_by_instance")
    public ResponseEntity<PageInfo<DevopsServiceVO>> pageByInstance(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例id")
            @RequestParam(value = "instance_id",required = false) Long instanceId,
            @ApiParam(value = "服务id")
            @RequestParam(value = "app_service_id",required = false) Long appServiceId,
            @ApiParam(value = "分页参数")
            @SortDefault(value = "id", direction = Sort.Direction.DESC)
            @ApiIgnore PageRequest pageRequest) {
        return Optional.ofNullable(devopsServiceService.pageByInstance(projectId, instanceId, pageRequest, appServiceId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException(ERROR_APP_K8S_SERVICE_QUERY));
    }
}