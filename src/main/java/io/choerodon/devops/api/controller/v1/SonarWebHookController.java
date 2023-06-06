package io.choerodon.devops.api.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/webhook/sonar")
public class SonarWebHookController {

//    @Autowired
//    private GitlabWebHookService gitlabWebHookService;
//
//    @Permission(permissionPublic = true)
//    @ApiOperation(value = "webhook转发")
//    @PostMapping
//    public ResponseEntity<Void> forwardGitlabWebHook(HttpServletRequest httpServletRequest, @RequestBody String body) {
//        gitlabWebHookService.forwardingEventToPortal(body, httpServletRequest.getHeader("X-Gitlab-Token"));
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
//
//    @Permission(permissionPublic = true)
//    @ApiOperation(value = "gitops webhook转发")
//    @PostMapping(value = "/git_ops")
//    public ResponseEntity<Void> gitOpsWebHook(HttpServletRequest httpServletRequest, @RequestBody String body) {
//        gitlabWebHookService.gitOpsWebHook(body, httpServletRequest.getHeader("X-Gitlab-Token"));
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
}
