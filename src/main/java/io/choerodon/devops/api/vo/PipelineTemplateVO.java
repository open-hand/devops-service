package io.choerodon.devops.api.vo;

import java.util.List;
import javax.persistence.Column;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.template.CiTemplateStageVO;
import io.choerodon.devops.infra.dto.CiTemplateCategoryDTO;

/**
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 17:01:24
 */
public class PipelineTemplateVO {


    @Encrypt
    private Long id;
    @ApiModelProperty(value = "流水线模板名称", required = true)
    private String name;
    @ApiModelProperty(value = "层级", required = true)
    private String sourceType;
    @ApiModelProperty(value = "层级Id", required = true)
    private Long sourceId;
    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    private Boolean builtIn;
    @ApiModelProperty(value = "关联语言id", required = true)
    @Encrypt
    private Long ciTemplateCategoryId;
    @ApiModelProperty(value = "是否启用", required = true)
    private Long enable;
    @ApiModelProperty(value = "版本命名规则", required = true)
    private String versionName;
    @ApiModelProperty(value = "流水线模板镜像地址", required = true)
    private String image;

    @ApiModelProperty("是否可中断")
    @Column(name = "is_interruptible")
    private Boolean interruptible;

    private List<CiTemplateStageVO> ciTemplateStageVOList;

    private CiTemplateCategoryDTO ciTemplateCategoryDTO;

    public Boolean getInterruptible() {
        return interruptible;
    }

    public void setInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
    }

    public CiTemplateCategoryDTO getCiTemplateCategoryDTO() {
        return ciTemplateCategoryDTO;
    }

    public void setCiTemplateCategoryDTO(CiTemplateCategoryDTO ciTemplateCategoryDTO) {
        this.ciTemplateCategoryDTO = ciTemplateCategoryDTO;
    }

    public List<CiTemplateStageVO> getCiTemplateStageVOList() {
        return ciTemplateStageVOList;
    }

    public void setCiTemplateStageVOList(List<CiTemplateStageVO> ciTemplateStageVOList) {
        this.ciTemplateStageVOList = ciTemplateStageVOList;
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

    public Long getEnable() {
        return enable;
    }

    public void setEnable(Long enable) {
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
}
