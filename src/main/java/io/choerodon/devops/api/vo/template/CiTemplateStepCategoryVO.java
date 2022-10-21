package io.choerodon.devops.api.vo.template;


import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * 流水线步骤模板分类(CiTemplateStepCategory)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */


public class CiTemplateStepCategoryVO {

    @Encrypt
    private Long id;

    @ApiModelProperty(value = "任务名称", required = true)
    @NotBlank
    @Size(max = 15)
    private String name;


    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    @NotNull
    private Boolean builtIn;

    @ApiModelProperty(value = "创建时间")
    private Date creationDate;

    @ApiModelProperty(value = "创建者")
    private Long createdBy;


    @ApiModelProperty(value = "关联模板的总数")
    private Long templateNumber;

    @ApiModelProperty("创建者")
    private IamUserDTO creator;

    public Boolean getBuiltIn() {
        return builtIn;
    }

    private List<CiTemplateStepVO> ciTemplateStepVOList;


    public List<CiTemplateStepVO> getCiTemplateStepVOList() {
        return ciTemplateStepVOList;
    }

    public void setCiTemplateStepVOList(List<CiTemplateStepVO> ciTemplateStepVOList) {
        this.ciTemplateStepVOList = ciTemplateStepVOList;
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

}

