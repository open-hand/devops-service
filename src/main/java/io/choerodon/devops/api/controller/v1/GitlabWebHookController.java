package io.choerodon.devops.api.controller.v1;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.GitlabWebHookService;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/webhook")
public class GitlabWebHookController {

    @Autowired
    private GitlabWebHookService gitlabWebHookService;


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

}
