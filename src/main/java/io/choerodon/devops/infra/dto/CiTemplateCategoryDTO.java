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
 * 流水线模板适用语言表(CiTemplateLanguage)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:17
 */

@ApiModel("流水线模板适用语言表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_ci_template_category")
public class CiTemplateCategoryDTO extends AuditDomain {
    private static final long serialVersionUID = 355683976509988264L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_SOURCE_TYPE = "sourceType";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "语言", required = true)
    @NotBlank
    private String category;


    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    @NotNull
    private Boolean builtIn;

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


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


}

