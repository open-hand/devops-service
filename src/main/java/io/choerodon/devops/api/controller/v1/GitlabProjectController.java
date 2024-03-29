package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.GitlabGroupService;
import io.choerodon.swagger.annotation.Permission;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/10/7 22:13
 */
@RestController
@RequestMapping("/v1/gitlab_projects")
public class GitlabProjectController {

    @Autowired
    private GitlabGroupService gitlabGroupService;

    @Permission(permissionPublic = true)
    @ApiOperation(value = "校验仓库是否还有剩余空间")
    @GetMapping("/check_repository_available")
    public ResponseEntity<Boolean> checkRepositoryAvailable(
            @ApiParam("gitlab group 名称")
            @RequestParam(name = "groupName") String groupName,
            @ApiParam("gitlab project 名称")
            @RequestParam(name = "projectName") String projectName,
            @ApiParam("认证token")
            @RequestParam(name = "token") String token) {
        return ResponseEntity.ok(gitlabGroupService.checkRepositoryAvailable(groupName, projectName, token));
    }
}
