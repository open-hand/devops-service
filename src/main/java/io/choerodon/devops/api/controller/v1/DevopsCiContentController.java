package io.choerodon.devops.api.controller.v1;

import io.choerodon.core.annotation.Permission;
import io.choerodon.devops.app.service.DevopsCiContentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:30
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/ci_contents")
public class DevopsCiContentController {
    private DevopsCiContentService devopsCiContentService;

    public DevopsCiContentController(DevopsCiContentService devopsCiContentService) {
        this.devopsCiContentService = devopsCiContentService;
    }
    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询项目下流水线最新的gitlab-ci配置文件")
    @GetMapping("/pipelines/{pipeline_id}")
    public ResponseEntity<String> queryLatestContent(
            @PathVariable(value = "project_id") Long projectId,
            @PathVariable(value = "pipeline_id") Long pipelineId) {
        return ResponseEntity.ok(devopsCiContentService.queryLatestContent(pipelineId));
    }
}
