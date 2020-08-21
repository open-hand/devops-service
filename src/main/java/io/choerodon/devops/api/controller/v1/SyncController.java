package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.SyncService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author scp
 * @date 2020/7/1
 * @description
 */
@RestController
@RequestMapping(value = "/sync")
public class SyncController {

    @Autowired
    private SyncService syncService;

    @Permission(permissionPublic = true)
    @ApiOperation(value = "同步创建gitlab用户")
    @GetMapping("/user")
    public ResponseEntity<Void> userWithOutGitlabUser() {
        syncService.userWithOutGitlabUser();
        return ResponseEntity.noContent().build();
    }
}
