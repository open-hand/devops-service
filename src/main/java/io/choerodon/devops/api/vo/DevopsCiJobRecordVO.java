package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.pipeline.*;
import io.choerodon.devops.api.vo.test.ApiTestTaskRecordVO;

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
    @ApiModelProperty("单元测试覆盖率")
    private String codeCoverage;
    //    @ApiModelProperty("详细信息")
//    private String metadata;
    @ApiModelProperty("是否有镜像扫描")
    private Boolean imageScan;
    //    @ApiModelProperty("任务记录关联的maven配置id")
//    private Long mavenSettingId;
    private Long commandId;

    @ApiModelProperty("ci生成jar包地址")
    private PipelineJarInfoVO pipelineJarInfo;
    @ApiModelProperty("ci任务产生的chart信息")
    private PipelineChartInfo pipelineChartInfo;
    @ApiModelProperty("ci任务产生的sonar信息")
    private PipelineSonarInfo pipelineSonarInfo;
    @ApiModelProperty("ci任务产生的镜像信息")
    private PipelineImageInfoVO pipelineImageInfo;
    @ApiModelProperty("ci任务产生的单元测试信息")
    private List<DevopsCiUnitTestReportVO> devopsCiUnitTestReportInfoList;
    @ApiModelProperty("人工卡点任务信息")
    private Audit audit;
    @ApiModelProperty("chart部署任务信息")
    private DeployInfo deployInfo;

    @ApiModelProperty("api测试记录id")
    @Encrypt
    private Long apiTestTaskRecordId;

    @Encrypt
    @ApiModelProperty("关联的配置id")
    private Long configId;

    private ApiTestTaskRecordVO apiTestTaskRecordVO;

    public ApiTestTaskRecordVO getApiTestTaskRecordVO() {
        return apiTestTaskRecordVO;
    }

    public void setApiTestTaskRecordVO(ApiTestTaskRecordVO apiTestTaskRecordVO) {
        this.apiTestTaskRecordVO = apiTestTaskRecordVO;
    }

    public Long getConfigId() {
        return configId;
    }

    public Long getApiTestTaskRecordId() {
        return apiTestTaskRecordId;
    }

    public void setApiTestTaskRecordId(Long apiTestTaskRecordId) {
        this.apiTestTaskRecordId = apiTestTaskRecordId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public DeployInfo getDeployInfo() {
        return deployInfo;
    }

    public void setDeployInfo(DeployInfo deployInfo) {
        this.deployInfo = deployInfo;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public List<DevopsCiUnitTestReportVO> getDevopsCiUnitTestReportInfoList() {
        return devopsCiUnitTestReportInfoList;
    }

    public void setDevopsCiUnitTestReportInfoList(List<DevopsCiUnitTestReportVO> devopsCiUnitTestReportInfoList) {
        this.devopsCiUnitTestReportInfoList = devopsCiUnitTestReportInfoList;
    }

    public PipelineSonarInfo getPipelineSonarInfo() {
        return pipelineSonarInfo;
    }

    public void setPipelineSonarInfo(PipelineSonarInfo pipelineSonarInfo) {
        this.pipelineSonarInfo = pipelineSonarInfo;
    }

    public PipelineImageInfoVO getPipelineImageInfo() {
        return pipelineImageInfo;
    }

    public void setPipelineImageInfo(PipelineImageInfoVO pipelineImageInfo) {
        this.pipelineImageInfo = pipelineImageInfo;
    }

    public PipelineChartInfo getPipelineChartInfo() {
        return pipelineChartInfo;
    }

    public void setPipelineChartInfo(PipelineChartInfo pipelineChartInfo) {
        this.pipelineChartInfo = pipelineChartInfo;
    }
//
//    public Long getMavenSettingId() {
//        return mavenSettingId;
//    }
//
//    public void setMavenSettingId(Long mavenSettingId) {
//        this.mavenSettingId = mavenSettingId;
//    }

    public Boolean getImageScan() {
        return imageScan;
    }

    public void setImageScan(Boolean imageScan) {
        this.imageScan = imageScan;
    }

    public PipelineJarInfoVO getPipelineJarInfo() {
        return pipelineJarInfo;
    }

    public void setPipelineJarInfo(PipelineJarInfoVO pipelineJarInfo) {
        this.pipelineJarInfo = pipelineJarInfo;
    }

//    public String getMetadata() {
//        return metadata;
//    }
//
//    public void setMetadata(String metadata) {
//        this.metadata = metadata;
//    }

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

    public String getCodeCoverage() {
        return codeCoverage;
    }

    public void setCodeCoverage(String codeCoverage) {
        this.codeCoverage = codeCoverage;
    }
}
