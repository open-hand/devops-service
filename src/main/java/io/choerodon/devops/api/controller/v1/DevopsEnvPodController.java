package io.choerodon.devops.api.controller.v1;

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
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.app.service.DevopsEnvPodService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Zenger on 2018/4/17.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/pods")
public class DevopsEnvPodController {

    @Autowired
    private DevopsEnvPodService devopsEnvPodService;

    /**
     * 分页查询环境下pod
     *
     * @param projectId   项目id
     * @param pageable    分页参数
     * @param searchParam 查询参数
     * @return page of DevopsEnvironmentPodVO
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "分页查询环境下pod")
    @CustomPageRequest
    @PostMapping(value = "/page_by_options")
    public ResponseEntity<Page<DevopsEnvPodVO>> pageByOptions(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageable,
            @Encrypt
            @ApiParam(value = "环境id")
            @RequestParam(value = "env_id", required = false) Long envId,
            @Encrypt
            @ApiParam(value = "应用id")
            @RequestParam(value = "app_service_id", required = false) Long appServiceId,
            @Encrypt
            @ApiParam(value = "实例id")
            @RequestParam(value = "instance_id", required = false) Long instanceId,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return ResponseEntity.ok(devopsEnvPodService.pageByOptions(
                projectId, envId, appServiceId, instanceId, pageable, searchParam));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "按类型分页查询环境下pod")
    @CustomPageRequest
    @PostMapping(value = "/page_by_kind")
    public ResponseEntity<Page<DevopsEnvPodVO>> pageByKind(
            @PathVariable(value = "project_id") Long projectId,
            @ApiIgnore PageRequest pageable,
            @Encrypt
            @RequestParam(value = "env_id") Long envId,
            @RequestParam(value = "kind") String kind,
            @RequestParam(value = "name") String name,
            @ApiParam(value = "查询参数")
            @RequestBody(required = false) String searchParam) {
        return ResponseEntity.ok(devopsEnvPodService.pageByKind(projectId, envId, kind, name, pageable, searchParam));
    }

    /**
     * 删除实例下面的pod
     *
     * @param envId 环境id
     * @param podId pod id
     * @return void
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER})
    @ApiOperation(value = "删除环境下的pod")
    @DeleteMapping("/{pod_id}")
    public ResponseEntity<Void> deleteEnvPod(
            @ApiParam(value = "项目id")
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "podId")
            @PathVariable(value = "pod_id") Long podId,
            @Encrypt
            @ApiParam(value = "环境id", required = true)
            @RequestParam(value = "env_id") Long envId) {
        devopsEnvPodService.deleteEnvPodById(projectId, envId, podId);
        return ResponseEntity.noContent().build();
    }
}
