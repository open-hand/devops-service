package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.GitlabUserService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author zmf
 * @since 2/3/20
 */
@RequestMapping("/v1/users")
@RestController
public class UserController {
    @Autowired
    private GitlabUserService gitlabUserService;

    /**
     * 重置用户的gitlab密码
     *
     * @param userId 猪齿鱼用户id
     * @return 重置后的密码
     */
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiOperation(value = "重置用户的gitlab密码")
    @PutMapping("/{user_id}/git_password")
    public ResponseEntity<String> resetUserGitlabPassword(
            @Encrypt
            @ApiParam(value = "猪齿鱼用户id", required = true)
            @PathVariable(value = "user_id") Long userId) {
        return new ResponseEntity<>(gitlabUserService.resetGitlabPassword(userId), HttpStatus.OK);
    }

    @ApiOperation("如果后台没有同步用户任务，触发异步同步用户任务")
    @Permission(level = ResourceLevel.SITE)
    @PostMapping("/trigger_syncing")
    public ResponseEntity<Void> triggerSyncingUser() {
        gitlabUserService.asyncHandleAllUsers();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
