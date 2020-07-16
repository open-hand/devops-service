package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/4/7 22:34
 */
public class CiCdJobRecordVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("gitlab job记录id")
    private Long gitlabJobId;
    @ApiModelProperty("流水线记录id")
    @Encrypt
    private Long pipelineRecordId;
    @ApiModelProperty("阶段名称")
    private String stage;
    @ApiModelProperty("触发用户")
    @Encrypt
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

    @ApiModelProperty("ci中返回sonar")
    private List<SonarContentVO> sonarContentVOS;

    private Boolean countersigned;
    private String appServiceName;
    private String envName;
    @Encrypt
    private Long appServiceId;
    @Encrypt
    private Long envId;
    private String version;
    private String instanceName;
    @Encrypt
    private Long taskId;
    private String instanceStatus;
    @Encrypt
    private Long instanceId;
    private Boolean envPermission;
    private List<PipelineUserVO> userDTOList;

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

    public Long getPipelineRecordId() {
        return pipelineRecordId;
    }

    public void setPipelineRecordId(Long pipelineRecordId) {
        this.pipelineRecordId = pipelineRecordId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public List<SonarContentVO> getSonarContentVOS() {
        return sonarContentVOS;
    }

    public void setSonarContentVOS(List<SonarContentVO> sonarContentVOS) {
        this.sonarContentVOS = sonarContentVOS;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Boolean getEnvPermission() {
        return envPermission;
    }

    public void setEnvPermission(Boolean envPermission) {
        this.envPermission = envPermission;
    }

    public List<PipelineUserVO> getUserDTOList() {
        return userDTOList;
    }

    public void setUserDTOList(List<PipelineUserVO> userDTOList) {
        this.userDTOList = userDTOList;
    }
}
