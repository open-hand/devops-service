package io.choerodon.devops.api.vo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.api.validator.annotation.AtMostSeveralFieldsNotEmpty;

/**
 * @author wanghao
 * @Date 2020/4/2 17:00
 */
@AtMostSeveralFieldsNotEmpty(fields = {"triggerRefs", "regexMatch", "exactMatch", "exactExclude"},
        message = "error.job.trigger.type.at.most.one")
@Table(name = "devops_ci_job")
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

    @ApiModelProperty("分支匹配模式/可以传入分支关键字和tags关键字,模糊匹配")
    private String triggerRefs;
    @ApiModelProperty("正则匹配模式")
    private String regexMatch;
    @ApiModelProperty("精确匹配模式")
    private String exactMatch;
    @ApiModelProperty("精确排除模式")
    private String exactExclude;

    @ApiModelProperty("详细信息")
    @NotEmpty(message = "error.job.metadata.cannot.be.null")
    private String metadata;

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

    public String getRegexMatch() {
        return regexMatch;
    }

    public void setRegexMatch(String regexMatch) {
        this.regexMatch = regexMatch;
    }

    public String getExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(String exactMatch) {
        this.exactMatch = exactMatch;
    }

    public String getExactExclude() {
        return exactExclude;
    }

    public void setExactExclude(String exactExclude) {
        this.exactExclude = exactExclude;
    }
}
