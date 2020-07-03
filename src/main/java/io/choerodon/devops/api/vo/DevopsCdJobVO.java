package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;

/**
 * @author wanghao
 * @since 2020/4/2 17:00
 */
public class DevopsCdJobVO {

    private Long id;
    @ApiModelProperty("任务名称")
    @NotEmpty(message = "error.job.name.cannot.be.null")
    private String name;
    @ApiModelProperty("runner镜像地址")
    private String image;
    @ApiModelProperty("阶段id")
    private Long cdStageId;
    @ApiModelProperty("流水线id")
    private Long pipelineId;
    @ApiModelProperty("任务类型")
    @NotEmpty(message = "error.job.type.cannot.be.null")
    private String type;
    @ApiModelProperty("触发类型对应的值")
    private String triggerValue;

    /**
     * {@link io.choerodon.devops.infra.enums.CiTriggerType}
     */
    @ApiModelProperty("触发类型")
    private String triggerType;

    @ApiModelProperty("详细信息")
    @NotEmpty(message = "error.job.metadata.cannot.be.null")
    private String metadata;

    @ApiModelProperty("是否上传共享目录的内容 / 默认为false")
    private Boolean toUpload;

    @ApiModelProperty("是否下载共享目录的内容 / 默认为false")
    private Boolean toDownload;

    private List<Long> cdAuditUserIds;
    private Boolean countersigned;
    private Long appServiceDeployId;
    private PipelineAppServiceDeployVO pipelineAppServiceDeployVO;
    private Long projectId;
    private Date lastUpdateDate;
    private Long objectVersionNumber;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getCdStageId() {
        return cdStageId;
    }

    public void setCdStageId(Long cdStageId) {
        this.cdStageId = cdStageId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Boolean getToUpload() {
        return toUpload;
    }

    public void setToUpload(Boolean toUpload) {
        this.toUpload = toUpload;
    }

    public Boolean getToDownload() {
        return toDownload;
    }

    public void setToDownload(Boolean toDownload) {
        this.toDownload = toDownload;
    }

    public List<Long> getCdAuditUserIds() {
        return cdAuditUserIds;
    }

    public void setCdAuditUserIds(List<Long> cdAuditUserIds) {
        this.cdAuditUserIds = cdAuditUserIds;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }

    public Long getAppServiceDeployId() {
        return appServiceDeployId;
    }

    public void setAppServiceDeployId(Long appServiceDeployId) {
        this.appServiceDeployId = appServiceDeployId;
    }

    public PipelineAppServiceDeployVO getPipelineAppServiceDeployVO() {
        return pipelineAppServiceDeployVO;
    }

    public void setPipelineAppServiceDeployVO(PipelineAppServiceDeployVO pipelineAppServiceDeployVO) {
        this.pipelineAppServiceDeployVO = pipelineAppServiceDeployVO;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
