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
    private Object id;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "任务分组id", required = true)
    @NotNull
    private Object groupId;

    @ApiModelProperty(value = "流水线模板镜像地址", required = true)
    @NotBlank
    private String runnerImages;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String sourceType;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Object sourceId;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    @NotNull
    private Object builtIn;

    @ApiModelProperty(value = "是否上传到共享目录", required = true)
    @NotNull
    private Object isToUpload;

    @ApiModelProperty(value = "是否下载到共享目录", required = true)
    @NotNull
    private Object isToDownload;


    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getGroupId() {
        return groupId;
    }

    public void setGroupId(Object groupId) {
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

    public Object getSourceId() {
        return sourceId;
    }

    public void setSourceId(Object sourceId) {
        this.sourceId = sourceId;
    }

    public Object getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Object builtIn) {
        this.builtIn = builtIn;
    }

    public Object getIsToUpload() {
        return isToUpload;
    }

    public void setIsToUpload(Object isToUpload) {
        this.isToUpload = isToUpload;
    }

    public Object getIsToDownload() {
        return isToDownload;
    }

    public void setIsToDownload(Object isToDownload) {
        this.isToDownload = isToDownload;
    }

}

