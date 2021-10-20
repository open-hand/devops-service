package io.choerodon.devops.api.controller.v1;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Boolean> checkRepositoryAvailable(@RequestParam(name = "groupName") String groupName,
                                                            @RequestParam(name = "projectName") String projectName,
                                                            @RequestParam(name = "token") String token) {
        return ResponseEntity.ok(gitlabGroupService.checkRepositoryAvailable(groupName, projectName, token));
    }
}
