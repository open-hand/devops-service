package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.DevopsCiContentService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/3 9:30
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_contents")
public class DevopsCiContentController {
    private final DevopsCiContentService devopsCiContentService;

    public DevopsCiContentController(DevopsCiContentService devopsCiContentService) {
        this.devopsCiContentService = devopsCiContentService;
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询项目下流水线最新的gitlab-ci配置文件")
    @GetMapping("/pipelines/{pipeline_token}/content.yaml")
    public ResponseEntity<String> queryLatestContent(
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "流水线的token")
            @PathVariable(value = "pipeline_token") String token) {
        return ResponseEntity.ok(devopsCiContentService.queryLatestContent(token));
    }
}
