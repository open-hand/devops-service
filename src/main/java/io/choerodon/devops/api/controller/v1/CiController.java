package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.core.iam.InitRoleCode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.app.service.ApplicationService;
import io.choerodon.devops.app.service.ApplicationVersionService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by younger on 2018/4/13.
 */

@RestController
@RequestMapping(value = "/ci")
public class CiController {

    private ApplicationService applicationService;
    private ApplicationVersionService applicationVersionService;

    public CiController(ApplicationService applicationService, ApplicationVersionService applicationVersionService) {
        this.applicationService = applicationService;
        this.applicationVersionService = applicationVersionService;
    }

    /**
     * 应用查询ci脚本文件
     *
     * @param token token
     * @param type  类型
     * @return File
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER},
            permissionPublic = true)
    @ApiOperation(value = "应用查询ci脚本文件")
    @GetMapping
    public ResponseEntity<String> queryFile(
            @ApiParam(value = "token")
            @RequestParam String token,
            @ApiParam(value = "类型")
            @RequestParam(required = false) String type) {
        return Optional.ofNullable(applicationService.queryFile(token, type))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.file.get"));
    }


    /**
     * 获取应用版本信息
     *
     * @param token   token
     * @param image   类型
     * @param version 版本
     * @param commit  commit
     * @param file    tgz包
     * @return File
     */
    @Permission(level = ResourceLevel.PROJECT,
            roles = {InitRoleCode.PROJECT_OWNER},
            permissionPublic = true)
    @ApiOperation(value = "获取应用版本信息")
    @PostMapping
    public ResponseEntity create(
            @ApiParam(value = "image", required = true)
            @RequestParam String image,
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "版本", required = true)
            @RequestParam String version,
            @ApiParam(value = "commit", required = true)
            @RequestParam String commit,
            @ApiParam(value = "taz包", required = true)
            @RequestParam MultipartFile file) {
        applicationVersionService.create(image, token, version, commit, file);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
