package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsJenkinsServerStatusCheckResponseVO;
import io.choerodon.devops.api.vo.DevopsJenkinsServerVO;
import io.choerodon.devops.api.vo.SearchVO;
import io.choerodon.devops.app.service.DevopsJenkinsServerService;
import io.choerodon.devops.infra.dto.DevopsJenkinsServerDTO;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/projects/{project_id}/jenkins_server")
public class DevopsJenkinsServerController {
    @Autowired
    private DevopsJenkinsServerService devopsJenkinsServerService;

    /**
     * 分页查询jenkins server
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询jenkins server")
    @CustomPageRequest
    @PostMapping("/page")
    public ResponseEntity<Page<DevopsJenkinsServerVO>> pageServer(@ApiParam(value = "项目ID", required = true)
                                                                  @PathVariable(value = "project_id") Long projectId,
                                                                  @ApiParam(value = "分页参数")
                                                                  @ApiIgnore
                                                                  @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageable,
                                                                  @RequestBody SearchVO searchVO) {
        return ResponseEntity.ok(devopsJenkinsServerService.pageServer(projectId, pageable, searchVO));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下可见的jenkins服务")
    @GetMapping("/all")
    public ResponseEntity<List<DevopsJenkinsServerDTO>> listAll(@ApiParam(value = "项目ID", required = true)
                                                                    @PathVariable(value = "project_id") Long projectId,
                                                                @RequestParam(required = false, defaultValue = "enabled") String status) {
        return ResponseEntity.ok(devopsJenkinsServerService.listAll(projectId, status));
    }

    /**
     * 查询jenkins server
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询jenkins server")
    @CustomPageRequest
    @GetMapping("/{jenkins_server_id}")
    public ResponseEntity<DevopsJenkinsServerVO> query(@ApiParam(value = "项目ID", required = true)
                                                       @PathVariable(value = "project_id") Long projectId,
                                                       @Encrypt @PathVariable("jenkins_server_id") Long jenkinsServerId) {
        return ResponseEntity.ok(devopsJenkinsServerService.query(projectId, jenkinsServerId));
    }

    /**
     * 创建jenkins server
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = " 创建jenkins server")
    @PostMapping
    public ResponseEntity<DevopsJenkinsServerVO> create(@ApiParam(value = "项目ID", required = true)
                                                        @PathVariable(value = "project_id") Long projectId,
                                                        @ApiParam(value = "server信息", required = true)
                                                        @RequestBody DevopsJenkinsServerVO devopsJenkinsServerVO) {
        return ResponseEntity.ok(devopsJenkinsServerService.create(projectId, devopsJenkinsServerVO));
    }

    /**
     * 更新jenkins server
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = " 更新jenkins server")
    @PutMapping
    public void update(@ApiParam(value = "项目ID", required = true)
                       @PathVariable(value = "project_id") Long projectId,
                       @ApiParam(value = "server信息", required = true)
                       @RequestBody DevopsJenkinsServerVO devopsJenkinsServerVO) {
        devopsJenkinsServerService.update(projectId, devopsJenkinsServerVO);
    }

    /**
     * 删除jenkins server
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除jenkins server")
    @DeleteMapping("/{jenkins_server_id}")
    public void delete(@ApiParam(value = "项目ID", required = true)
                       @PathVariable(value = "project_id") Long projectId,
                       @ApiParam(value = "server信息", required = true)
                       @Encrypt @PathVariable("jenkins_server_id") Long jenkinsServerId) {
        devopsJenkinsServerService.delete(projectId, jenkinsServerId);
    }

    /**
     * 启用jenkins server
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = " 启用jenkins server")
    @PostMapping("/{jenkins_server_id}/enable")
    public void enable(@ApiParam(value = "项目ID", required = true)
                       @PathVariable(value = "project_id") Long projectId,
                       @ApiParam(value = "server信息", required = true)
                       @Encrypt @PathVariable("jenkins_server_id") Long jenkinsServerId) {
        devopsJenkinsServerService.enable(projectId, jenkinsServerId);
    }

    /**
     * 停用jenkins server
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = " 停用jenkins server")
    @PostMapping("/{jenkins_server_id}/disable")
    public void disable(@ApiParam(value = "项目ID", required = true)
                        @PathVariable(value = "project_id") Long projectId,
                        @ApiParam(value = "server信息", required = true)
                        @Encrypt @PathVariable("jenkins_server_id") Long jenkinsServerId) {
        devopsJenkinsServerService.disable(projectId, jenkinsServerId);
    }

    /**
     * 检查jenkins server状态
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "检查jenkins server状态")
    @PostMapping("/check_connection")
    public ResponseEntity<DevopsJenkinsServerStatusCheckResponseVO> checkConnection(@ApiParam(value = "项目ID", required = true)
                                                                                    @PathVariable(value = "project_id") Long projectId,
                                                                                    @ApiParam(value = "server信息", required = true)
                                                                                    @RequestBody DevopsJenkinsServerVO devopsJenkinsServerVO) {
        return ResponseEntity.ok(devopsJenkinsServerService.checkConnection(projectId, devopsJenkinsServerVO));
    }

    /**
     * 检查jenkins 名称是否存在
     * 如果是更新操作，需要带上jenkinsServerId
     */
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "检查jenkins 名称是否存在")
    @PostMapping("/check_name_exists")
    public ResponseEntity<Boolean> checkNameExists(@ApiParam(value = "项目ID", required = true)
                                                   @PathVariable(value = "project_id") Long projectId,
                                                   @ApiParam(value = "server信息", required = true)
                                                   @Encrypt @RequestParam(value = "jenkins_server_id", required = false) Long jenkinsServerId,
                                                   @RequestParam(value = "name") String name) {
        return ResponseEntity.ok(devopsJenkinsServerService.checkNameExists(projectId, jenkinsServerId, name));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionPublic = true)
    @ApiOperation(value = "下载jenkins插件")
    @GetMapping("/plugins/download")
    public ResponseEntity<Resource> downloadPlugin(@ApiParam(value = "项目ID", required = true)
                                                   @PathVariable(value = "project_id") Long projectId) {
        return devopsJenkinsServerService.downloadPlugin();
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "jenkins插件最新版本")
    @GetMapping("/plugins/lasted_version")
    public ResponseEntity<String> queryPluginLastedVersion(@ApiParam(value = "项目ID", required = true)
                                                           @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsJenkinsServerService.queryPluginLastedVersion());
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionPublic = true)
    @ApiOperation(value = "下载jenkins相关图片资源")
    @GetMapping("/images/{name}")
    public ResponseEntity<Resource> downloadImage(@ApiParam(value = "项目ID", required = true)
                                                  @PathVariable(value = "project_id") Long projectId,
                                                  @PathVariable(value = "name") String name) {
        return devopsJenkinsServerService.downloadImage(name);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionLogin = true)
    @ApiOperation(value = "查询jenkins使用指引md文档")
    @GetMapping("/user_guide")
    public ResponseEntity<String> queryUserGuide(@ApiParam(value = "项目ID", required = true)
                                                 @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(devopsJenkinsServerService.queryUserGuide(projectId));
    }

}
