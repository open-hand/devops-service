package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.*;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author wanghao
 * @since 2020/4/2 17:00
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_cd_job")
public class DevopsCdJobDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("任务名称")
    private String name;
    @ApiModelProperty("流水线id")
    private Long pipelineIid;
    @ApiModelProperty("阶段id")
    private Long stageId;
    @ApiModelProperty("任务类型")
    private String type;
    /**
     * {@link io.choerodon.devops.infra.enums.CiTriggerType}
     */
    @ApiModelProperty("触发类型")
    private String triggerType;
    @ApiModelProperty("触发分支")
    private String triggerRefs;
    @ApiModelProperty("镜像地址")
    private String image;
    @ApiModelProperty("是否上传共享目录的内容 / 默认为false")
    @Column(name = "is_to_upload")
    private Boolean toUpload;
    @ApiModelProperty("是否下载共享目录的内容 / 默认为false")
    @Column(name = "is_to_download")
    private Boolean toDownload;
    @ApiModelProperty("详细信息")
    private String metadata;
    @ApiModelProperty("应用部署Id")
    private Long appServiceDeployId;
    @ApiModelProperty("是否会签")
    @Column(name = "is_countersigned")
    private Boolean countersigned;
    @ApiModelProperty("项目ID")
    private Long projectId;

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

    public Long getPipelineIid() {
        return pipelineIid;
    }

    public void setPipelineIid(Long pipelineIid) {
        this.pipelineIid = pipelineIid;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerRefs() {
        return triggerRefs;
    }

    public void setTriggerRefs(String triggerRefs) {
        this.triggerRefs = triggerRefs;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Long getAppServiceDeployId() {
        return appServiceDeployId;
    }

    public void setAppServiceDeployId(Long appServiceDeployId) {
        this.appServiceDeployId = appServiceDeployId;
    }

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

}
