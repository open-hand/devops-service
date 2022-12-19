package io.choerodon.devops.api.controller.v1;


import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.SettingsService;
import io.choerodon.swagger.annotation.Permission;

@RestController("settingsController.v1")
@RequestMapping("/v1/projects/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @ApiOperation("查询gitlab重置密码链接")
    @Permission(permissionLogin = true)
    @GetMapping("/gitlab_reset_password_url")
    public ResponseEntity<String> getGitlabResetPasswordUrl() {
        return ResponseEntity.ok(settingsService.getGitlabResetPasswordUrl());
    }
}
