package io.choerodon.devops.api.vo.template;

import java.util.Date;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 流水线模板适用语言表(CiTemplateLanguage)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:17
 */


public class CiTemplateCategoryVO {

    @Encrypt
    private Long id;

    @ApiModelProperty(value = "语言", required = true)
    @NotBlank
    private String category;

    @ApiModelProperty(value = "层级", required = true)
    @NotBlank
    private String sourceType;

    @ApiModelProperty(value = "创建时间")
    private Date creationDate;

    @ApiModelProperty(value = "创建者")
    private Long createdBy;

    @ApiModelProperty(value = "关联模板的总数")
    private Long templateNumber;

    @ApiModelProperty("创建者")
    private IamUserDTO creator;
    @ApiModelProperty(value = "分类图标base64格式", required = false)
    private String image;
    private Boolean builtIn;

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getTemplateNumber() {
        return templateNumber;
    }

    public void setTemplateNumber(Long templateNumber) {
        this.templateNumber = templateNumber;
    }

    public IamUserDTO getCreator() {
        return creator;
    }

    public void setCreator(IamUserDTO creator) {
        this.creator = creator;
    }
}

