package io.choerodon.devops.api.vo.template;

/**
 * Created by wangxiang on 2021/12/3
 */

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;

public class CiTemplatePipelineVO {

    @Encrypt
    private Long id;

    @ApiModelProperty("流水线名称")
    @NotNull(message = "流水线模板名称不能为空")
    private String name;

    @ApiModelProperty("层级")
    private String sourceType;

    @ApiModelProperty("层级id")
    private Long sourceId;

    @ApiModelProperty("是否预置")
    private Boolean builtIn;

    @ApiModelProperty("模板分类Id")
    @NotNull
    @Encrypt
    private Long ciTemplateCategoryId;

    @ApiModelProperty("是否启用")
    private Boolean enable;

    @ApiModelProperty("版本命令规则")
    private String versionName;

    @ApiModelProperty("镜像地址")
    private String image;

    @ApiModelProperty("是否可中断")
    @Column(name = "is_interruptible")
    private Boolean interruptible;

    @ApiModelProperty("适用变成语言对象")
    private CiTemplateCategoryVO ciTemplateCategoryVO;

    @ApiModelProperty(value = "创建时间")
    private Date creationDate;

    @ApiModelProperty(value = "创建者")
    private Long createdBy;

    @ApiModelProperty("创建者")
    private IamUserDTO creator;

    @ApiModelProperty("流水线模板的阶段模板")
    private List<CiTemplateStageVO> templateStageVOS;

    private List<CiTemplateVariableVO> ciTemplateVariableVOS;

    @ApiModelProperty("版本策略 false 平台默认 true 自定义")
    private Boolean versionStrategy;

    public Boolean getInterruptible() {
        return interruptible;
    }

    public void setInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
    }

    public List<CiTemplateVariableVO> getCiTemplateVariableVOS() {
        return ciTemplateVariableVOS;
    }

    public void setCiTemplateVariableVOS(List<CiTemplateVariableVO> ciTemplateVariableVOS) {
        this.ciTemplateVariableVOS = ciTemplateVariableVOS;
    }

    public List<CiTemplateStageVO> getTemplateStageVOS() {
        return templateStageVOS;
    }

    public void setTemplateStageVOS(List<CiTemplateStageVO> templateStageVOS) {
        this.templateStageVOS = templateStageVOS;
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

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }


    public Long getCiTemplateCategoryId() {
        return ciTemplateCategoryId;
    }

    public void setCiTemplateCategoryId(Long ciTemplateCategoryId) {
        this.ciTemplateCategoryId = ciTemplateCategoryId;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public CiTemplateCategoryVO getCiTemplateCategoryVO() {
        return ciTemplateCategoryVO;
    }

    public void setCiTemplateCategoryVO(CiTemplateCategoryVO ciTemplateCategoryVO) {
        this.ciTemplateCategoryVO = ciTemplateCategoryVO;
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

    public IamUserDTO getCreator() {
        return creator;
    }

    public void setCreator(IamUserDTO creator) {
        this.creator = creator;
    }

    public Boolean getVersionStrategy() {
        return versionStrategy;
    }

    public void setVersionStrategy(Boolean versionStrategy) {
        this.versionStrategy = versionStrategy;
    }
}
