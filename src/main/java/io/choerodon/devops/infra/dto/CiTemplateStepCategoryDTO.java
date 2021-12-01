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
 * 流水线步骤模板分类(CiTemplateStepCategory)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */

@ApiModel("流水线步骤模板分类")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_step_category")
public class CiTemplateStepCategoryDTO extends AuditDomain {
    private static final long serialVersionUID = -25740459261648611L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SOURCE_TYPE = "sourceType";
    public static final String FIELD_SOURCE_ID = "sourceId";
    public static final String FIELD_BUILT_IN = "builtIn";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String sourceType;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Long sourceId;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    @NotNull
    private Long builtIn;


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

    public Long getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Long builtIn) {
        this.builtIn = builtIn;
    }

}

