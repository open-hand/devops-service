package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.choerodon.base.annotation.Permission;
import io.choerodon.devops.app.service.AppServiceVersionService;
import io.choerodon.devops.app.service.AppSevriceService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by younger on 2018/4/13.
 */

@RestController
@RequestMapping(value = "/ci")
public class CiController {

    private AppSevriceService applicationService;
    private AppServiceVersionService appServiceVersionService;

    public CiController(AppSevriceService applicationService, AppServiceVersionService appServiceVersionService) {
        this.applicationService = applicationService;
        this.appServiceVersionService = appServiceVersionService;
    }

    /**
     * 服务查询ci脚本文件
     *
     * @param token token
     * @param type  类型
     * @return File
     */
    @Permission(
            permissionPublic = true)
    @ApiOperation(value = "服务查询ci脚本文件")
    @GetMapping
    public ResponseEntity<String> queryFile(
            @ApiParam(value = "token")
            @RequestParam String token,
            @ApiParam(value = "类型")
            @RequestParam(required = false) String type) {
        return Optional.ofNullable(applicationService.queryFile(token, type))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * 获取服务版本信息
     *
     * @param token   @param image   类型
     * @param version 版本
     * @param commit  commit
     * @param file    tgz包
     * @return File
     */
    @Permission(permissionPublic = true)
    @ApiOperation(value = "获取服务版本信息")
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
        appServiceVersionService.create(image, token, version, commit, file);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
