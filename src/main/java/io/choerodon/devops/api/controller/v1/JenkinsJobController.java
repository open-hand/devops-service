package io.choerodon.devops.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.jenkins.JenkinsJobVO;
import io.choerodon.devops.app.service.JenkinsJobService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/3 9:22
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/jobs")
public class JenkinsJobController {

    @Autowired
    private JenkinsJobService jenkinsJobService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询项目下所有JenkinsJob")
    @GetMapping("/")
    public ResponseEntity<List<JenkinsJobVO>> listAll(
            @ApiParam(value = "项目Id", required = true)
            @PathVariable(value = "project_id") Long projectId) {
        return ResponseEntity.ok(jenkinsJobService.listAll(projectId));
    }
}
