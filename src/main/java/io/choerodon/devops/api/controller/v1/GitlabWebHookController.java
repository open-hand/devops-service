package io.choerodon.devops.api.controller.v1;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.GitlabWebHookService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/webhook")
public class GitlabWebHookController {

    @Autowired
    private GitlabWebHookService gitlabWebHookService;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Permission(permissionPublic = true)
    @ApiOperation(value = "webhook转发")
    @PostMapping
    public ResponseEntity forwardGitlabWebHook(HttpServletRequest httpServletRequest, @RequestBody String body) {
        gitlabWebHookService.forwardingEventToPortal(body, httpServletRequest.getHeader("X-Gitlab-Token"));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "gitops webhook转发")
    @PostMapping(value = "/git_ops")
    public ResponseEntity gitOpsWebHook(HttpServletRequest httpServletRequest, @RequestBody String body) {
        gitlabWebHookService.gitOpsWebHook(body, httpServletRequest.getHeader("X-Gitlab-Token"));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 查询自动化测试应用实例状态
     * @param testReleases
     */
    @ApiOperation(value = "查询自动化测试应用实例状态")
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER,
                    InitRoleCode.PROJECT_MEMBER})
    @PostMapping("/get_test_status")
    public void getTestStatus(
            @ApiParam(value = "releaseName", required = true)
            @RequestBody Map<Long, List<String>> testReleases) {
        applicationInstanceService.getTestAppStatus(testReleases);
    }
}
