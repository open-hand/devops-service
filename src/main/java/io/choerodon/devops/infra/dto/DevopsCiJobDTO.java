package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author wanghao
 * @since 2020/4/2 17:00
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_job")
public class DevopsCiJobDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("任务名称")
    private String name;

    @ApiModelProperty("镜像地址")
    private String image;

    @ApiModelProperty("阶段id")
    private Long ciStageId;
    @ApiModelProperty("流水线id")
    private Long ciPipelineId;

    @ApiModelProperty("分组类型")
    private String groupType;

    @ApiModelProperty("任务类型")
    private String type;
    @ApiModelProperty("触发类型对应的值")
    private String triggerValue;
    @ApiModelProperty("job的并发数")
    private Integer parallel;

    /**
     * {@link io.choerodon.devops.infra.enums.CiTriggerType}
     */
    @ApiModelProperty("触发类型")
    private String triggerType;

    @ApiModelProperty("是否上传共享目录的内容 / 默认为false")
    @Column(name = "is_to_upload")
    private Boolean toUpload;

    @ApiModelProperty("是否下载共享目录的内容 / 默认为false")
    @Column(name = "is_to_download")
    private Boolean toDownload;

    @ApiModelProperty("脚本类型任务的自定义脚本")
    private String script;

    @ApiModelProperty("任务配置id")
    private Long configId;

    @ApiModelProperty("启动延时")
    private Integer startIn;
    @ApiModelProperty("任务tags")
    private String tags;

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getStartIn() {
        return startIn;
    }

    public void setStartIn(Integer startIn) {
        this.startIn = startIn;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Integer getParallel() {
        return parallel;
    }

    public void setParallel(Integer parallel) {
        this.parallel = parallel;
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
