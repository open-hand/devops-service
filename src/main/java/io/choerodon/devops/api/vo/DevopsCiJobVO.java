package io.choerodon.devops.api.vo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author wanghao
 * @since 2020/4/2 17:00
 */
public class DevopsCiJobVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("任务名称")
    @NotEmpty(message = "error.job.name.cannot.be.null")
    private String name;

    @ApiModelProperty("runner镜像地址")
    private String image;

    @ApiModelProperty("阶段id")
    private Long ciStageId;

    @ApiModelProperty("流水线id")
    private Long ciPipelineId;

    @ApiModelProperty("任务类型")
    @NotEmpty(message = "error.job.type.cannot.be.null")
    private String type;

    @ApiModelProperty("触发分支")
    @NotEmpty(message = "error.job.triggerRefs.cannot.be.null")
    private String triggerRefs;

    @ApiModelProperty("详细信息")
    @NotEmpty(message = "error.job.metadata.cannot.be.null")
    private String metadata;

    @ApiModelProperty("是否上传共享目录的内容 / 默认为false")
    private Boolean toUpload = Boolean.FALSE;

    @ApiModelProperty("是否下载共享目录的内容 / 默认为false")
    private Boolean toDownload = Boolean.FALSE;

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

    public Long getCiStageId() {
        return ciStageId;
    }

    public void setCiStageId(Long ciStageId) {
        this.ciStageId = ciStageId;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggerRefs() {
        return triggerRefs;
    }

    public void setTriggerRefs(String triggerRefs) {
        this.triggerRefs = triggerRefs;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
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
}
