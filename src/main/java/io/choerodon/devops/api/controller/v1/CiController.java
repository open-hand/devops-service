package io.choerodon.devops.api.controller.v1;

import java.util.Date;
import java.util.Optional;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.CiPipelineImageVO;
import io.choerodon.devops.api.vo.ImageRepoInfoVO;
import io.choerodon.devops.api.vo.SonarInfoVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiUnitTestResultVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by younger on 2018/4/13.
 */
@RestController
@RequestMapping(value = "/ci")
public class CiController {
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
    @Autowired
    private DevopsCiPipelineSonarService devopsCiPipelineSonarService;
    @Autowired
    private DevopsCiUnitTestReportService devopsCiUnitTestReportService;

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
            @ApiParam(value = "GitLab流水线id")
            @RequestParam(value = "gitlabPipelineId", required = false) Long gitlabPipelineId,
            @ApiParam(value = "job_name")
            @RequestParam(value = "jobName", required = false) String jobName,
            @ApiParam(value = "taz包", required = true)
            @RequestParam MultipartFile file,
            @RequestParam String ref) {
        appServiceVersionService.create(image, harborConfigId, repoType, token, version, commit, file, ref, gitlabPipelineId, jobName);
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
            @RequestParam(value = "nexus_repo_id", required = false) Long nexusRepoId,
            @ApiParam(value = "猪齿鱼的CI的JOB纪录的id", required = true)
            @RequestParam("job_id") Long jobId,
            @ApiParam(value = "制品库id", required = true)
            @RequestParam("sequence") Long sequence,
            @ApiParam(value = "maven仓库地址", required = true)
            @RequestParam(value = "maven_repo_url", required = false) String mavenRepoUrl,
            @ApiParam(value = "maven仓库用户名", required = true)
            @RequestParam(value = "username", required = false) String username,
            @ApiParam(value = "maven仓库用户密码", required = true)
            @RequestParam(value = "password", required = false) String password,
            @ApiParam(value = "GitLab流水线id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "job_name", required = true)
            @RequestParam(value = "job_name") String jobName,
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "pom文件", required = true)
            @RequestParam MultipartFile file) {
        ciPipelineMavenService.createOrUpdate(nexusRepoId, jobId, sequence, gitlabPipelineId, jobName, token, file, mavenRepoUrl, username, password);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "存储sonar信息")
    @PostMapping("/save_sonar_info")
    public ResponseEntity<Void> saveSonarInfo(
            @ApiParam(value = "GitLab流水线id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "job_name", required = true)
            @RequestParam(value = "job_name") String jobName,
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "scanner_type", required = true)
            @RequestParam(value = "scanner_type") String scannerType) {
        devopsCiPipelineSonarService.saveSonarInfo(gitlabPipelineId, jobName, token, scannerType);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionLogin = true)
    @ApiOperation(value = "判断平台是否有配置sonarqube")
    @GetMapping("/has_default_sonar")
    public ResponseEntity<Boolean> hasDefaultSonarqubeConfig() {
        return ResponseEntity.ok(StringUtils.hasText(sonarqubeUrl));
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
            @RequestParam(value = "job_id", required = false) Long jobId,
            @ApiParam(value = "start_date")
            @RequestParam(value = "start_date") Date startDate,
            @ApiParam(value = "end_date")
            @RequestParam(value = "end_date") Date endDate,
            @ApiParam(value = "job_name", required = true)
            @RequestParam(value = "job_name") String jobName,
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "json文件", required = false)
            @RequestParam MultipartFile file) {
        devopsImageScanResultService.resolveImageScanJson(gitlabPipelineId, jobId, startDate, endDate, file, token, jobName);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "上传单元测试报告")
    @PostMapping("/upload_unit_test")
    public ResponseEntity<Void> uploadUnitTest(
            @ApiParam(value = "GitLab流水线id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "job_name", required = true)
            @RequestParam(value = "job_name") String jobName,
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "token", required = true)
            @RequestParam(value = "type") String type,
            @ApiParam(value = "测试报告", required = true)
            @RequestParam MultipartFile file,
            @ApiParam(value = "测试结果（如果传了则使用用户上传的结果）")
                    DevopsCiUnitTestResultVO devopsCiUnitTestResultVO) {
        devopsCiUnitTestReportService.uploadUnitTest(gitlabPipelineId, jobName, token, type, file, devopsCiUnitTestResultVO);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询制品仓库信息", hidden = true)
    @GetMapping("/rewrite_repo_info_script")
    public ResponseEntity<ImageRepoInfoVO> queryRewriteRepoInfoScript(
            @RequestParam(value = "project_id") Long projectId,
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "仓库类型", required = true)
            @RequestParam(value = "repo_type") String repoType,
            @ApiParam(value = "制品库id", required = true)
            @RequestParam(value = "repo_id") Long repoId) {
        return ResponseEntity.ok(ciPipelineImageService.queryRewriteRepoInfoScript(projectId, token, repoType, repoId));
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询制品仓库信息", hidden = true)
    @GetMapping("/image_repo_info")
    public ResponseEntity<ImageRepoInfoVO> queryImageRepoInfo(
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "GitLab流水线id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId) {
        return ResponseEntity.ok(ciPipelineImageService.queryImageRepoInfo(token, gitlabPipelineId));
    }

}
