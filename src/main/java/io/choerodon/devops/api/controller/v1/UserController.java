package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.annotation.Permission;
import io.choerodon.core.enums.ResourceType;
import io.choerodon.devops.app.service.GitlabUserService;

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
    @Permission(type = ResourceType.SITE, permissionLogin = true)
    @ApiOperation(value = "重置用户的gitlab密码")
    @PutMapping("/{user_id}/git_password")
    public ResponseEntity<String> resetUserGitlabPassword(
            @ApiParam(value = "猪齿鱼用户id", required = true)
            @PathVariable(value = "user_id") Long userId) {
        return new ResponseEntity<>(gitlabUserService.resetGitlabPassword(userId), HttpStatus.OK);
    }
}
