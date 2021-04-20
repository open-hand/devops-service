package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.Optional;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiPipelineImageVO;
import io.choerodon.devops.api.vo.SonarInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by younger on 2018/4/13.
 */
@RestController
@RequestMapping(value = "/ci")
public class CiController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${devops.ci.default.image}")
    private String defaultCiImage;

    @Value("${services.sonarqube.url:#{null}}")
    private String sonarqubeUrl;

    @Value("${services.sonarqube.username:#{null}}")
    private String userName;
    @Value("${services.sonarqube.password:#{null}}")
    private String password;

    private final AppServiceService applicationService;
    private final AppServiceVersionService appServiceVersionService;
    private final CiPipelineImageService ciPipelineImageService;
    private final CiPipelineMavenService ciPipelineMavenService;

    @Autowired
    private DevopsImageScanResultService devopsImageScanResultService;

    public CiController(AppServiceService applicationService,
                        AppServiceVersionService appServiceVersionService,
                        CiPipelineMavenService ciPipelineMavenService,
                        CiPipelineImageService ciPipelineImageService) {
        this.applicationService = applicationService;
        this.appServiceVersionService = appServiceVersionService;
        this.ciPipelineImageService = ciPipelineImageService;
        this.ciPipelineMavenService = ciPipelineMavenService;
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
            @ApiParam(value = "repo_type", required = true)
            @RequestParam(value = "repo_type") String repoType,
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "版本", required = true)
            @RequestParam String version,
            @ApiParam(value = "commit", required = true)
            @RequestParam String commit,
            @ApiParam(value = "taz包", required = true)
            @RequestParam MultipartFile file,
            @RequestParam String ref) {
        appServiceVersionService.create(image, harborConfigId, repoType, token, version, commit, file, ref);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询CI流水线默认的镜像地址")
    @GetMapping("/default_image")
    public ResponseEntity<String> queryDefaultCiImageUrl() {
        return ResponseEntity.ok(defaultCiImage);
    }


    @Permission(permissionPublic = true)
    @ApiOperation(value = "存储镜像的元数据")
    @PostMapping("/record_image")
    public ResponseEntity<Void> createImageRecord(@RequestBody @Valid CiPipelineImageVO ciPipelineImageVO) {
        ciPipelineImageService.createOrUpdate(ciPipelineImageVO);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "存储Jar包的元数据")
    @PostMapping("/save_jar_metadata")
    public ResponseEntity<Void> saveJarMetaData(
            @ApiParam(value = "制品库id", required = true)
            @RequestParam("nexus_repo_id") Long nexusRepoId,
            @ApiParam(value = "猪齿鱼的CI的JOB纪录的id", required = true)
            @RequestParam("job_id") Long jobId,
            @ApiParam(value = "制品库id", required = true)
            @RequestParam("sequence") Long sequence,
            @ApiParam(value = "GitLab流水线id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "job_name", required = true)
            @RequestParam(value = "job_name") String jobName,
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "pom文件", required = true)
            @RequestParam MultipartFile file) {
        ciPipelineMavenService.createOrUpdate(nexusRepoId, jobId, sequence, gitlabPipelineId, jobName, token, file);
        return ResponseEntity.ok().build();
    }


    @Permission(permissionLogin = true)
    @ApiOperation(value = "判断平台是否有配置sonarqube")
    @GetMapping("/has_default_sonar")
    public ResponseEntity<Boolean> hasDefaultSonarqubeConfig() {
        return ResponseEntity.ok(!StringUtils.isEmpty(sonarqubeUrl));
    }

    @Permission(level = ResourceLevel.ORGANIZATION, permissionWithin = true)
    @GetMapping("/sonar_default")
    @ApiOperation("质量管理用/查询sonar默认配置 / 结果可能改为空的对象，字段值可能为空")
    public ResponseEntity<SonarInfoVO> getSonarDefault() {
        return ResponseEntity.ok(new SonarInfoVO(userName, password, sonarqubeUrl));
    }


    @Permission(permissionPublic = true)
    @ApiOperation(value = "解析ci阶段镜像扫描产生的json文件")
    @PostMapping("/resolve_image_scan_json")
    public ResponseEntity<Void> resolveImageScanJson(
            @ApiParam(value = "GitLab流水线id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "猪齿鱼的CI的JOB纪录的id", required = false)
            @RequestParam("job_id") Long jobId,
            @ApiParam(value = "start_date")
            @RequestParam(value = "start_date") Date startDate,
            @ApiParam(value = "end_date")
            @RequestParam(value = "end_date") Date endDate,
            @ApiParam(value = "json文件", required = true)
            @RequestParam MultipartFile file) {
        devopsImageScanResultService.resolveImageScanJson(gitlabPipelineId, jobId, startDate, endDate, file);
        return ResponseEntity.ok().build();
    }

}
