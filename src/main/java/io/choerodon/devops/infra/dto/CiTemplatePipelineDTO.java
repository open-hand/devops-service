package io.choerodon.devops.infra.dto;

/**
 * Created by wangxiang on 2021/12/3
 */

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_template_pipeline")
public class CiTemplatePipelineDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("流水线名称")
    private String name;

    @ApiModelProperty("层级")
    private String sourceType;

    @ApiModelProperty("层级id")
    private Long sourceId;

    @ApiModelProperty("是否预置")
    private Boolean builtIn;

    @ApiModelProperty("分类id")
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

    public Boolean getInterruptible() {
        return interruptible;
    }

    public void setInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
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
}
