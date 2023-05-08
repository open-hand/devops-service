package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.api.vo.host.DevopsHostAppVO;
import io.choerodon.devops.app.service.DevopsHostAppService;
import io.choerodon.mybatis.pagehelper.annotation.PageableDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Set;

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
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "主机id", required = true)
            @RequestParam(value = "host_id", required = false) Long hostId,
            @ApiParam(value = "制品类型", required = true)
            @RequestParam(value = "rdupm_type", required = false) String rdupmType,
            @ApiParam(value = "操作类型", required = true)
            @RequestParam(value = "operation_type", required = false) String operationType,
            @ApiParam(value = "搜索参数", required = true)
            @RequestParam(value = "params", required = false) String params,
            @ApiParam(value = "应用名称")
            @RequestParam(value = "name", required = false) String name,
            @Encrypt
            @RequestParam(value = "app_id", required = false) Long appId,
            @ApiIgnore @PageableDefault() PageRequest pageRequest) {
        return ResponseEntity.ok(devopsHostAppService.pagingAppByHost(projectId, hostId, pageRequest, rdupmType, operationType, params, name, appId));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询主机下的应用实例详情")
    @GetMapping("/apps/{app_id}")
    public ResponseEntity<DevopsHostAppVO> queryAppById(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "主机应用id", required = true)
            @Encrypt
            @PathVariable(value = "app_id") Long id) {
        return ResponseEntity.ok(devopsHostAppService.queryAppById(projectId, id));
    }

    @ApiOperation("删除主机应用")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping("/{host_id}/apps/{app_id}")
    public ResponseEntity<Void> deleteById(@PathVariable("project_id") Long projectId,
                                           @ApiParam(value = "主机id", required = true)
                                           @Encrypt
                                           @PathVariable("host_id") Long hostId,
                                           @Encrypt
                                           @ApiParam(value = "主机应用id", required = true)
                                           @PathVariable("app_id") Long appId) {
        devopsHostAppService.deleteById(projectId, hostId, appId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation("重启主机应用")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PutMapping("/{host_id}/apps/{app_id}")
    public ResponseEntity<Void> restart(@PathVariable("project_id") Long projectId,
                                        @ApiParam(value = "主机id", required = true)
                                        @Encrypt
                                        @PathVariable("host_id") Long hostId,
                                        @Encrypt
                                        @ApiParam(value = "主机应用id", required = true)
                                        @PathVariable("app_id") Long appId) {
        devopsHostAppService.restart(projectId, hostId, appId);
        return ResponseEntity.noContent().build();
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验名称唯一")
    @GetMapping("/apps/check_name")
    public ResponseEntity<Boolean> checkNameUnique(
            @PathVariable("project_id") Long projectId,
            @RequestParam(value = "name") String name,
            @Encrypt @RequestParam(value = "host_id") Long hostId,
            @ApiParam(value = "应用id，更新应用时才需要传", required = true)
            @Encrypt @RequestParam(value = "app_id", required = false) Long appId) {
        return ResponseEntity.ok(devopsHostAppService.checkNameUnique(projectId, hostId, appId, name));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验code唯一")
    @GetMapping("/apps/check_code")
    public ResponseEntity<Boolean> checkCodeUnique(
            @PathVariable("project_id") Long projectId,
            @ApiParam(value = "应用编码", required = true)
            @Encrypt @RequestParam(value = "host_id") Long hostId,
            @RequestParam(value = "code") String code) {
        return ResponseEntity.ok(devopsHostAppService.checkCodeUnique(projectId, hostId, null, code));
    }

    @ApiOperation("查询引用了主机应用作为替换对象的流水线信息")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/apps/{app_id}/pipeline_reference")
    public ResponseEntity<List<PipelineInstanceReferenceVO>> queryPipelineReference(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "应用ID", required = true)
            @PathVariable(value = "app_id") Long appId) {
        return ResponseEntity.ok(devopsHostAppService.queryPipelineReferenceHostApp(projectId, appId));
    }

    @ApiOperation("查询主机设置的应用工作目录")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{host_id}/list_work_dirs")
    public ResponseEntity<Set<String>> listWorkDirs(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @Encrypt
            @ApiParam(value = "主机id", required = true)
            @PathVariable(value = "host_id") Long hostId) {
        return ResponseEntity.ok(devopsHostAppService.listWorkDirs(projectId, hostId));
    }
}
