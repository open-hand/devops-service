package io.choerodon.devops.api.vo.template;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 17:12:44
 */
public class CiTemplateJobVO {


    private Long id;
    @ApiModelProperty(value = "任务名称", required = true)
    private String name;
    @ApiModelProperty(value = "任务分组id", required = true)
    private Long groupId;
    @ApiModelProperty(value = "流水线模板镜像地址", required = true)
    private String runnerImages;
    @ApiModelProperty(value = "层级", required = true)
    private String sourceType;
    @ApiModelProperty(value = "层级Id", required = true)
    private Long sourceId;
    @ApiModelProperty(value = "是否上传到共享目录", required = true)
    private Long toUpload;
    @ApiModelProperty(value = "是否下载到共享目录", required = true)
    private Long toDownload;
    @ApiModelProperty(value = "任务镜像", required = true)
    private String image;

    private String type;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    private Boolean builtIn;

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getRunnerImages() {
        return runnerImages;
    }

    public void setRunnerImages(String runnerImages) {
        this.runnerImages = runnerImages;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getToUpload() {
        return toUpload;
    }

    public void setToUpload(Long toUpload) {
        this.toUpload = toUpload;
    }

    public Long getToDownload() {
        return toDownload;
    }

    public void setToDownload(Long toDownload) {
        this.toDownload = toDownload;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
