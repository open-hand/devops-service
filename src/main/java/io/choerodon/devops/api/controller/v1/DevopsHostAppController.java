package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.app.service.DevopsHostAppService;
import io.choerodon.mybatis.pagehelper.annotation.PageableDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/23 16:21
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/hosts")
public class DevopsHostAppController {

    @Autowired
    private DevopsHostAppService devopsHostAppService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询主机下的应用实例")
    @GetMapping("/apps/paging")
    @CustomPageRequest
    public ResponseEntity<Page<DevopsHostAppVO>> pagingAppByHost(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "主机", required = true)
            @RequestParam(value = "host_id", required = false) Long hostId,
            @RequestParam(value = "rdupmType", required = false) String rdupmType,
            @RequestParam(value = "operationType", required = false) String operationType,
            @RequestParam(value = "params", required = false) String params,
            @ApiIgnore @PageableDefault() PageRequest pageRequest) {
        return ResponseEntity.ok(devopsHostAppService.pagingAppByHost(projectId, hostId, pageRequest, rdupmType, operationType, params));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询主机下的应用实例详情")
    @GetMapping("/apps/{app_id}")
    public ResponseEntity<DevopsHostAppVO> queryAppById(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "实例ID", required = true)
            @PathVariable(value = "app_id") Long id) {
        return ResponseEntity.ok(devopsHostAppService.queryAppById(projectId, id));
    }

    @ApiOperation("删除主机应用")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{host_id}/apps/{app_id}")
    public ResponseEntity<Void> deleteById(@ApiParam(value = "项目id", required = true)
                                           @PathVariable("project_id") Long projectId,
                                           @ApiParam(value = "主机id", required = true)
                                           @Encrypt
                                           @PathVariable("host_id") Long hostId,
                                           @Encrypt
                                           @PathVariable("app_id") Long appId) {
        devopsHostAppService.deleteById(projectId, hostId, appId);
        return ResponseEntity.noContent().build();
    }
}
