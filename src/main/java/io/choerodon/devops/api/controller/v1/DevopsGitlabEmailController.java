package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.GitlabUserService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@RestController
@RequestMapping(value = "/gitlab/email")
public class DevopsGitlabEmailController {

    @Autowired
    private GitlabUserService gitlabUserService;

    /**
     * 校验用户邮箱是否在gitlab已存在
     *
     * @param email 用户邮箱
     * @return Boolean
     */
    @Permission(
            permissionPublic = true)
    @ApiOperation(value = "校验用户邮箱是否在gitlab已存在")
    @GetMapping(value = "/check")
    public ResponseEntity<Boolean> doesEmailExists(
            @ApiParam(value = "用户邮箱", required = true)
            @RequestParam(value = "email") String email) {
        return ResponseEntity.ok(gitlabUserService.doesEmailExists(email));
    }
}
