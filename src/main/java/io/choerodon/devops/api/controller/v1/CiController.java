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
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.pipeline.CiResponseVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiUnitTestResultVO;
import io.choerodon.devops.app.eventhandler.pipeline.exec.CommandOperator;
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
    @Autowired
    private CommandOperator commandOperator;
    @Autowired
    private CiAuditRecordService ciAuditRecordService;
    @Autowired
    private DevopsCiJobRecordService devopsCiJobRecordService;
    @Autowired
    private CiPipelineVlunScanRecordRelService ciPipelineVlunScanRecordRelService;
    @Autowired
    private CiService ciService;

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
            @RequestParam(value = "harbor_config_id") Long harborConfigId,
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
            @RequestParam String ref,
            @RequestParam(value = "helm_repo_id", required = false) Long helmRepoId,
            @RequestParam(value = "gitlab_user_id", required = false) Long gitlabUserId) {
        appServiceVersionService.create(image,
                harborConfigId,
                repoType,
                token,
                version,
                commit,
                file,
                ref,
                gitlabPipelineId,
                jobName,
                helmRepoId,
                gitlabUserId);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "发布应用服务版本")
    @PostMapping("/app_version")
    public ResponseEntity<Void> publishAppVersion(
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
            @RequestParam String ref) {
        appServiceVersionService.publishAppVersion(token, version, commit, ref, gitlabPipelineId, jobName);
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

//    @Permission(permissionPublic = true)
//    @ApiOperation(value = "存储Jar包的元数据")
//    @PostMapping("/save_jar_metadata")
//    public ResponseEntity<Void> saveJarMetaData(
//            @ApiParam(value = "制品库id", required = true)
//            @RequestParam(value = "nexus_repo_id", required = false) Long nexusRepoId,
//            @ApiParam(value = "猪齿鱼的CI的JOB纪录的id", required = true)
//            @RequestParam("job_id") Long jobId,
//            @ApiParam(value = "制品库id", required = true)
//            @RequestParam("sequence") Long sequence,
//            @ApiParam(value = "maven仓库地址", required = true)
//            @RequestParam(value = "maven_repo_url", required = false) String mavenRepoUrl,
//            @ApiParam(value = "maven仓库用户名", required = true)
//            @RequestParam(value = "username", required = false) String username,
//            @ApiParam(value = "maven仓库用户密码", required = true)
//            @RequestParam(value = "password", required = false) String password,
//            @ApiParam(value = "GitLab流水线id", required = true)
//            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
//            @ApiParam(value = "job_name", required = true)
//            @RequestParam(value = "job_name") String jobName,
//            @ApiParam(value = "token", required = true)
//            @RequestParam String token,
//            @ApiParam(value = "版本", required = true)
//            @RequestParam String version,
//            @ApiParam(value = "pom文件", required = true)
//            @RequestParam MultipartFile file) {
//        ciPipelineMavenService.createOrUpdate(nexusRepoId, jobId, sequence, gitlabPipelineId, jobName, token, file, mavenRepoUrl, username, password, version);
//        return ResponseEntity.ok().build();
//    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "存储Jar包的元数据")
    @PostMapping("/save_jar_info")
    public ResponseEntity<Void> saveJarInfo(
            @ApiParam(value = "制品库id", required = true)
            @RequestParam(value = "nexus_repo_id", required = false) Long nexusRepoId,
//            @ApiParam(value = "猪齿鱼的CI的JOB纪录的id", required = true)
//            @RequestParam("mvn_settings_id") Long mvnSettingsId,
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
            @ApiParam(value = "版本", required = true)
            @RequestParam String version,
            @ApiParam(value = "pom文件", required = true)
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(value = "group_id", required = false) String groupId,
            @RequestParam(value = "artifact_id", required = false) String artifactId,
            @RequestParam(value = "jar_version", required = false) String jarVersion,
            @RequestParam(value = "packaging", required = false) String packaging
    ) {
        ciPipelineMavenService.createOrUpdateJarInfo(nexusRepoId,
//                mvnSettingsId,
                sequence,
                gitlabPipelineId,
                jobName,
                token,
                file,
                mavenRepoUrl,
                username,
                password,
                version,
                groupId,
                artifactId,
                jarVersion,
                packaging);
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
    @ApiOperation(value = "上传漏洞扫描结果")
    @PostMapping("/upload_vuln_result")
    public ResponseEntity<Void> uploadVulnResult(
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @RequestParam(value = "job_name") String jobName,
            @RequestParam String token,
            @RequestParam(value = "branch_name") String branchName,
            @RequestParam(value = "config_id", required = false) Long configId,
            @ApiParam(value = "扫描结果json", required = true)
            @RequestParam MultipartFile file) {
        ciPipelineVlunScanRecordRelService.uploadVulnResult(gitlabPipelineId, jobName, branchName, token, configId, file);
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

    @Permission(permissionPublic = true)
    @ApiOperation(value = "执行命令", hidden = true)
    @PostMapping("/exec_command")
    public ResponseEntity<CiResponseVO> execCommand(
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "GitLab流水线id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "GitLab Jobid", required = true)
            @RequestParam(value = "gitlab_job_id") Long gitlabJobId,
            @ApiParam(value = "部署配置id", required = true)
            @RequestParam(value = "config_id") Long configId,
            @ApiParam(value = "指令类型", required = true)
            @RequestParam(value = "command_type") String commandType) {
        return ResponseEntity.ok(commandOperator.executeCommandByType(token, gitlabPipelineId, gitlabJobId, configId, commandType));
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "流水线runner中查询主机部署命令执行状态", hidden = true)
    @PostMapping("/host_command_status")
    public ResponseEntity<CiResponseVO> hostCommandStatus(
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "GitLab流水线id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "commandId", required = true)
            @RequestParam(value = "command_id") Long commandId) {
        return ResponseEntity.ok(commandOperator.getHostCommandStatus(token, gitlabPipelineId, commandId));
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "更新job 关联的api测试执行记录信息", hidden = true)
    @PostMapping("/update_api_test_task_record_info")
    public ResponseEntity<Void> updateApiTestTaskRecordInfo(@ApiParam(value = "token", required = true)
                                                            @RequestParam String token,
                                                            @ApiParam(value = "GitLab Jobid", required = true)
                                                            @RequestParam(value = "gitlab_job_id") Long gitlabJobId,
                                                            @ApiParam(value = "configId", required = true)
                                                            @RequestParam(value = "config_id") Long configId,
                                                            @ApiParam(value = "测试任务执行记录id")
                                                            @RequestParam(value = "api_test_task_record_id") Long apiTestTaskRecordId) {
        devopsCiJobRecordService.updateApiTestTaskRecordInfo(token, gitlabJobId, configId, apiTestTaskRecordId);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询人工卡点任务审核状态", hidden = true)
    @PostMapping("/audit_status")
    public ResponseEntity<CiAuditResultVO> queryAuditStatus(
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "GitLab流水线id", required = true)
            @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
            @ApiParam(value = "job_name", required = true)
            @RequestParam(value = "job_name") String jobName) {
        return ResponseEntity.ok(ciAuditRecordService.queryAuditStatus(token, gitlabPipelineId, jobName));
    }

    @Permission(permissionWithin = true)
    @ApiOperation(value = "校验项目并返回触发job的用户id", hidden = true)
    @GetMapping("/check_and_get_trigger_user_id")
    public ResponseEntity<Long> checkAndGetTriggerUserId(@ApiParam("应用token")
                                                         @RequestParam("token") String token,
                                                         @ApiParam("gitlab job id")
                                                         @RequestParam("gitlab_job_id") Long gitlabJobId) {
        return ResponseEntity.ok(devopsCiJobRecordService.checkAndGetTriggerUserId(token, gitlabJobId));
    }

    @Permission(permissionPublic = false)
    @ApiOperation(value = "查询sonar质量门执行结果", hidden = true)
    @GetMapping("/get_sonar_quality_gate_result")
    public ResponseEntity<Boolean> getSonarQualityGateScanResult(@ApiParam(value = "GitLab流水线id", required = true)
                                                                 @RequestParam(value = "gitlab_pipeline_id") Long gitlabPipelineId,
                                                                 @ApiParam(value = "job_name", required = true)
                                                                 @RequestParam String token) {
        return ResponseEntity.ok(devopsCiPipelineSonarService.getSonarQualityGateScanResult(gitlabPipelineId, token));
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "api测试job触发通知", hidden = true)
    @PostMapping("/test_result_notify")
    public ResponseEntity<Void> testResultNotify(@ApiParam("应用token")
                                                 @RequestParam("token") String token,
                                                 @ApiParam("gitlab job id")
                                                 @RequestParam("gitlab_job_id") Long gitlabJobId,
                                                 @ApiParam("成功率")
                                                 @RequestParam("success_rate") String successRate) {
        devopsCiJobRecordService.testResultNotify(token, gitlabJobId, successRate);
        return ResponseEntity.ok().build();
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询制品仓库信息", hidden = true)
    @GetMapping("/npm_repo_info")
    public ResponseEntity<NpmRepoInfoVO> queryNpmRepoInfo(
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "repo_id", required = true)
            @RequestParam(value = "repo_id") Long repoId) {
        return ResponseEntity.ok(ciPipelineImageService.queryNpmRepoInfo(token, repoId));
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询配置文件信息", hidden = true)
    @GetMapping("/config_file")
    public ResponseEntity<String> queryConfigFileById(
            @ApiParam(value = "token", required = true)
            @RequestParam String token,
            @ApiParam(value = "config_file_id", required = true)
            @RequestParam(value = "config_file_id") Long configFileId) {
        return ResponseEntity.ok(ciService.queryConfigFileById(token, configFileId));
    }
}
