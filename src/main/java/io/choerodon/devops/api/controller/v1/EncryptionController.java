package io.choerodon.devops.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.devops.app.service.EncryptService;
import io.choerodon.swagger.annotation.Permission;


/**
 * @author scp
 * @date 2021-01-26 17:47
 */
@RestController
@RequestMapping(value = "/v1/encrypt")
public class EncryptionController {

    private final EncryptService encryptService;

    public EncryptionController(EncryptService encryptService) {
        this.encryptService = encryptService;
    }

    @Permission(permissionLogin = true)
    @ApiOperation("加密")
    @PostMapping
    public ResponseEntity<Map<String, String>> encryptIds(@RequestBody List<String> ids) {
        return ResponseEntity.ok(encryptService.encryptIds(ids));
    }
}
