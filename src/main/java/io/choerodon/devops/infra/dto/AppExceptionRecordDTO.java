package io.choerodon.devops.infra.dto;

import java.util.Date;
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
 * chart应用异常信息记录表(AppExceptionRecord)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-05-10 11:17:13
 */

@ApiModel("chart应用异常信息记录表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_app_exception_record")
public class AppExceptionRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_PROJECT_ID = "projectId";
    public static final String FIELD_APP_ID = "appId";
    public static final String FIELD_ENV_ID = "envId";
    public static final String FIELD_RESOURCE_TYPE = "resourceType";
    public static final String FIELD_RESOURCE_NAME = "resourceName";
    public static final String FIELD_START_DATE = "startDate";
    public static final String FIELD_END_DATE = "endDate";
    public static final String FIELD_DOWNTIME = "downtime";
    private static final long serialVersionUID = 417610389394717762L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "项目id", required = true)
    @NotNull
    private Long projectId;

    @ApiModelProperty(value = "应用id", required = true)
    @NotNull
    private Long appId;

    @ApiModelProperty(value = "环境id", required = true)
    @NotNull
    private Long envId;

    @ApiModelProperty(value = "资源类型", required = true)
    @NotBlank
    private String resourceType;

    @ApiModelProperty(value = "资源名称", required = true)
    @NotBlank
    private String resourceName;

    @ApiModelProperty(value = "异常开始时间", required = true)
    @NotNull
    private Date startDate;

    @ApiModelProperty(value = "异常结束时间")
    private Date endDate;

    @ApiModelProperty(value = "是否是停机状态", required = true)
    @NotNull
    private Boolean downtime;

    public AppExceptionRecordDTO() {
    }

    public AppExceptionRecordDTO(@NotNull Long projectId,
                                 @NotNull Long appId,
                                 @NotNull Long envId,
                                 @NotBlank String resourceType,
                                 @NotBlank String resourceName,
                                 @NotNull Date startDate,
                                 @NotNull Boolean downtime) {
        this.projectId = projectId;
        this.appId = appId;
        this.envId = envId;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.startDate = startDate;
        this.downtime = downtime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Boolean getDowntime() {
        return downtime;
    }

    public void setDowntime(Boolean downtime) {
        this.downtime = downtime;
    }
}

