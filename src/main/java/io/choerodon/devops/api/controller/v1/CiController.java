package io.choerodon.devops.api.controller.v1;

import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.AppServiceVersionService;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by younger on 2018/4/13.
 */
@RestController
@RequestMapping(value = "/ci")
public class CiController {
    @Value("${devops.ci.default.image}")
    private String defaultCiImage;

    @Value("${services.sonarqube.url:}")
    private String sonarqubeUrl;

    private final AppServiceService applicationService;
    private final AppServiceVersionService appServiceVersionService;
    private final DevopsCiJobService devopsCiJobService;

    public CiController(AppServiceService applicationService,
                        DevopsCiJobService devopsCiJobService,
                        AppServiceVersionService appServiceVersionService) {
        this.applicationService = applicationService;
        this.appServiceVersionService = appServiceVersionService;
        this.devopsCiJobService = devopsCiJobService;
    }

    /**
     * 服务查询ci脚本文件
     *
     * @param token token
     * @return File
     */
    @Permission(
            permissionPublic = true)
    @ApiOperation(value = "根据应用服务的Token和类型查询某个应用服务用于ci的脚本文件")
    @GetMapping
    public ResponseEntity<String> queryFile(
            @ApiParam(value = "token")
            @RequestParam String token) {
        return Optional.ofNullable(applicationService.queryFile(token))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * 创建应用服务版本
     *
     * @param image   类型
     * @param token   应用服务的token
     * @param version 版本
     * @param commit  commit
     * @param file    tgz包
     * @return File
     */
    @Permission(permissionPublic = true)
    @ApiOperation(value = "创建应用服务版本")
    @PostMapping
    public ResponseEntity<Void> create(
            @ApiParam(value = "image", required = true)
            @RequestParam String image,
            @ApiParam(value = "harbor_config_id", required = true)
            @RequestParam(value = "harbor_config_id") String harborConfigId,
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "版本", required = true)
            @RequestParam String version,
            @ApiParam(value = "commit", required = true)
            @RequestParam String commit,
            @ApiParam(value = "taz包", required = true)
            @RequestParam MultipartFile file) {
        appServiceVersionService.create(image, harborConfigId, token, version, commit, file);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询CI流水线默认的镜像地址")
    @GetMapping("/default_image")
    public ResponseEntity<String> queryDefaultCiImageUrl() {
        return ResponseEntity.ok(defaultCiImage);
    }

    @Permission(permissionLogin = true)
    @ApiOperation(value = "判断平台是否有配置sonarqube")
    @GetMapping("/has_default_sonar")
    public ResponseEntity<Boolean> hasDefaultSonarqubeConfig() {
        return ResponseEntity.ok(!StringUtils.isEmpty(sonarqubeUrl));
    }

    /**
     * CI校验上传软件包的信息
     *
     * @param token        应用服务token
     * @param commit       ci的commit值
     * @param ciPipelineId 流水线id
     * @param ciJobId      流水线的job id
     * @param artifactName 软件包名称
     * @return true表示通过校验， 未通过校验则会抛出{@link io.choerodon.core.exception.FeignException}
     */
    @Permission(permissionWithin = true)
    @ApiOperation("CI过程上传软件包校验信息, 大小不得大于200Mi")
    @PostMapping("/check_artifact_info")
    public ResponseEntity<Boolean> checkJobArtifactInfo(
            @ApiParam(value = "应用服务token", required = true)
            @RequestParam(value = "token") String token,
            @ApiParam(value = "此次ci的commit", required = true)
            @RequestParam(value = "commit") String commit,
            @ApiParam(value = "gitlab内置的流水线id", required = true)
            @RequestParam(value = "ci_pipeline_id") Long ciPipelineId,
            @ApiParam(value = "gitlab内置的jobId", required = true)
            @RequestParam(value = "ci_job_id") Long ciJobId,
            @ApiParam(value = "ci流水线定义的软件包名称", required = true)
            @RequestParam(value = "artifact_name") String artifactName,
            @ApiParam(value = "文件字节数")
            @RequestParam(value = "file_byte_size") Long fileByteSize) {
        return ResponseEntity.ok(devopsCiJobService.checkJobArtifactInfo(token, commit, ciPipelineId, ciJobId, artifactName, fileByteSize));
    }


    /**
     * CI过程保存软件包信息  200 表示ok， 400表示错误
     *
     * @param token        应用服务token
     * @param commit       ci的commit值
     * @param ciPipelineId 流水线id
     * @param ciJobId      流水线的job id
     * @param artifactName 软件包名称
     * @param fileUrl      软件包文件地址
     */
    @Permission(permissionWithin = true)
    @ApiOperation("CI过程保存软件包信息")
    @PostMapping("/save_artifact")
    @ResponseStatus(HttpStatus.OK)
    public void saveJobArtifactInfo(
            @ApiParam(value = "应用服务token", required = true)
            @RequestParam(value = "token") String token,
            @ApiParam(value = "此次ci的commit", required = true)
            @RequestParam(value = "commit") String commit,
            @ApiParam(value = "gitlab内置的流水线id", required = true)
            @RequestParam(value = "ci_pipeline_id") Long ciPipelineId,
            @ApiParam(value = "gitlab内置的jobId", required = true)
            @RequestParam(value = "ci_job_id") Long ciJobId,
            @ApiParam(value = "ci流水线定义的软件包名称", required = true)
            @RequestParam(value = "artifact_name") String artifactName,
            @ApiParam(value = "软件包地址", required = true)
            @RequestParam(value = "file_url") String fileUrl) {
        devopsCiJobService.saveArtifactInformation(token, commit, ciPipelineId, ciJobId, artifactName, fileUrl);
    }
}
