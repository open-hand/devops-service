package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.pipeline.PipelineChartInfo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/7 22:34
 */
public class DevopsCiJobRecordVO {
    @Encrypt
    private Long id;

    @ApiModelProperty("gitlab job记录id")
    private Long gitlabJobId;

    @Encrypt
    @ApiModelProperty("流水线记录id")
    private Long ciPipelineRecordId;
    @ApiModelProperty("阶段名称")
    private String stage;

    @Encrypt
    @ApiModelProperty("触发用户")
    private Long triggerUserId;
    @ApiModelProperty("任务类型")
    private String type;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("job状态")
    private String status;
    @ApiModelProperty("job开始时间")
    private Date startedDate;
    @ApiModelProperty("job结束时间")
    private Date finishedDate;
    @ApiModelProperty("job执行时间")
    private Long durationSeconds;
    @ApiModelProperty("release阶段生成chart的版本")
    private String chartVersion;

    private String sonarScannerType;

    private String codeCoverage;

    @ApiModelProperty("ci中返回sonar")
    private List<SonarContentVO> sonarContentVOS;

    @ApiModelProperty("详细信息")
    private String metadata;

    /**
     * ci流水线生成镜像下载命令
     */
    private String downloadImage;

    /**
     *  ci生成npm包地址
     */
    private String downloadNpm;

    /**
     * ci生成jar包地址
     */
    private DownloadMavenJarVO downloadMavenJarVO;

    /**
     * 是否有镜像扫描
     */
    private Boolean imageScan;

    private Long mavenSettingId;

    @ApiModelProperty("ci任务产生的chart信息")
    private PipelineChartInfo pipelineChartInfo;
//    @ApiModelProperty("ci任务产生的sonar信息")
//    private PipelineChartInfo pipelineSonarInfo;
//    @ApiModelProperty("ci任务产生的镜像信息")
//    private PipelineChartInfo pipelineImageInfo;


    public PipelineChartInfo getPipelineChartInfo() {
        return pipelineChartInfo;
    }

    public void setPipelineChartInfo(PipelineChartInfo pipelineChartInfo) {
        this.pipelineChartInfo = pipelineChartInfo;
    }

    public Long getMavenSettingId() {
        return mavenSettingId;
    }

    public void setMavenSettingId(Long mavenSettingId) {
        this.mavenSettingId = mavenSettingId;
    }

    public Boolean getImageScan() {
        return imageScan;
    }

    public void setImageScan(Boolean imageScan) {
        this.imageScan = imageScan;
    }

    public DownloadMavenJarVO getDownloadMavenJarVO() {
        return downloadMavenJarVO;
    }

    public void setDownloadMavenJarVO(DownloadMavenJarVO downloadMavenJarVO) {
        this.downloadMavenJarVO = downloadMavenJarVO;
    }

    public String getDownloadImage() {
        return downloadImage;
    }

    public void setDownloadImage(String downloadImage) {
        this.downloadImage = downloadImage;
    }


    public String getDownloadNpm() {
        return downloadNpm;
    }

    public void setDownloadNpm(String downloadNpm) {
        this.downloadNpm = downloadNpm;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public List<SonarContentVO> getSonarContentVOS() {
        return sonarContentVOS;
    }

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public void setSonarContentVOS(List<SonarContentVO> sonarContentVOS) {
        this.sonarContentVOS = sonarContentVOS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabJobId() {
        return gitlabJobId;
    }

    public void setGitlabJobId(Long gitlabJobId) {
        this.gitlabJobId = gitlabJobId;
    }

    public Long getCiPipelineRecordId() {
        return ciPipelineRecordId;
    }

    public void setCiPipelineRecordId(Long ciPipelineRecordId) {
        this.ciPipelineRecordId = ciPipelineRecordId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Long getTriggerUserId() {
        return triggerUserId;
    }

    public void setTriggerUserId(Long triggerUserId) {
        this.triggerUserId = triggerUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSonarScannerType() {
        return sonarScannerType;
    }

    public void setSonarScannerType(String sonarScannerType) {
        this.sonarScannerType = sonarScannerType;
    }

    public String getCodeCoverage() {
        return codeCoverage;
    }

    public void setCodeCoverage(String codeCoverage) {
        this.codeCoverage = codeCoverage;
    }
}
