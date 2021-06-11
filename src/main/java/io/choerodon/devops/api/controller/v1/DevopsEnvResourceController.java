package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.DevopsEnvResourceService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/11 10:15
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/resources")
public class DevopsEnvResourceController {

    @Autowired
    private DevopsEnvResourceService devopsEnvResourceService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取资源json详情")
    @GetMapping(value = "/detail_json")
    public ResponseEntity<Object> queryDetailsByKindAndName(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @RequestParam(value = "env_id") Long envId,
            @RequestParam(value = "kind") String kind,
            @RequestParam(value = "name") String name
    ) {
        return ResponseEntity.ok(devopsEnvResourceService.queryDetailsByKindAndName(envId, kind, name));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取资源yaml")
    @GetMapping(value = "/yaml")
    public ResponseEntity<String> queryYamlById(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @RequestParam(value = "env_id") Long envId,
            @RequestParam(value = "workload_id") Long workLoadId,
            @RequestParam(value = "type") String type
    ) {
        return ResponseEntity.ok(devopsEnvResourceService.queryYamlById(envId, workLoadId, type));
    }
}
