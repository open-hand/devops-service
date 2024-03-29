package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 流水线任务模板表(CiTemplateJob)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:15
 */

@ApiModel("流水线任务模板表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_job")
public class CiTemplateJobDTO extends AuditDomain {
    private static final long serialVersionUID = -16826131118494043L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_GROUP_ID = "groupId";
    public static final String FIELD_RUNNER_IMAGES = "runnerImages";
    public static final String FIELD_SOURCE_TYPE = "sourceType";
    public static final String FIELD_SOURCE_ID = "sourceId";
    public static final String FIELD_BUILT_IN = "builtIn";
    public static final String FIELD_IS_TO_UPLOAD = "isToUpload";
    public static final String FIELD_IS_TO_DOWNLOAD = "isToDownload";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "任务分组id", required = true)
    @NotNull
    private Long groupId;

    @ApiModelProperty(value = "流水线模板镜像地址", required = true)
    @NotBlank
    private String image;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String sourceType;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Long sourceId;

    @ApiModelProperty(value = "任务类型", required = true)
    @NotNull
    private String type;

    @ApiModelProperty(value = "是否上传到共享目录", required = true)
    @NotNull
    private Boolean toUpload;

    @ApiModelProperty(value = "是否下载到共享目录", required = true)
    @NotNull
    private Boolean toDownload;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    @NotNull
    private Boolean builtIn;
    private String script;

    @ApiModelProperty("job的并发数")
    private Integer parallel;

    public Integer getParallel() {
        return parallel;
    }

    public void setParallel(Integer parallel) {
        this.parallel = parallel;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

