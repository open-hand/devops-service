package io.choerodon.devops.api.vo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

public class DevopsCdJobRecordVO {
    @Encrypt
    private Long id;
    private String name;
    @Encrypt
    private Long stageRecordId;
    private String type;
    private String status;
    private String triggerType;
    private String triggerValue;
    private Long projectId;
    private String metadata;
    @ApiModelProperty("是否会签")
    private Integer countersigned;
    private String executionTime;
    @Encrypt
    private Long jobId;

    @ApiModelProperty("任务顺序")
    private Long sequence;

    private Date startedDate;
    private Date finishedDate;

    private Long durationSeconds;

    //自动部署记录详情
    private CdAuto cdAuto;
    private Audit audit;
    private Long deployInfoId;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public CdAuto getCdAuto() {
        return cdAuto;
    }

    public void setCdAuto(CdAuto cdAuto) {
        this.cdAuto = cdAuto;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }
    // 审核模式 指定审核人员 已审核人员 审核状态

    public class Audit {
        private List<IamUserDTO> appointUsers;
        private List<IamUserDTO> reviewedUsers;
        private String status;

        public List<IamUserDTO> getAppointUsers() {
            return appointUsers;
        }

        public void setAppointUsers(List<IamUserDTO> appointUsers) {
            this.appointUsers = appointUsers;
        }

        public List<IamUserDTO> getReviewedUsers() {
            return reviewedUsers;
        }

        public void setReviewedUsers(List<IamUserDTO> reviewedUsers) {
            this.reviewedUsers = reviewedUsers;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public class CdAuto {
        private String envName;
        private String appServiceName;
        private String appServiceVersion;
        private String instanceName;
        @Encrypt
        private Long instanceId;
        @Encrypt
        private Long appServiceId;
        @Encrypt
        private Long envId;

        public Long getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(Long instanceId) {
            this.instanceId = instanceId;
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

        public String getEnvName() {
            return envName;
        }

        public void setEnvName(String envName) {
            this.envName = envName;
        }

        public String getAppServiceName() {
            return appServiceName;
        }

        public void setAppServiceName(String appServiceName) {
            this.appServiceName = appServiceName;
        }

        public String getAppServiceVersion() {
            return appServiceVersion;
        }

        public void setAppServiceVersion(String appServiceVersion) {
            this.appServiceVersion = appServiceVersion;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStageRecordId() {
        return stageRecordId;
    }

    public void setStageRecordId(Long stageRecordId) {
        this.stageRecordId = stageRecordId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Integer getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Integer countersigned) {
        this.countersigned = countersigned;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
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

    public void setJobExecuteTime() {
        if (this.startedDate == null || this.finishedDate == null) return;
        LocalDateTime start = startedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime finished = finishedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.durationSeconds = Duration.between(start, finished).getSeconds();
    }

    public Long getDeployInfoId() {
        return deployInfoId;
    }

    public void setDeployInfoId(Long deployInfoId) {
        this.deployInfoId = deployInfoId;
    }
}
