package io.choerodon.devops.api.vo.template;

/**
 * Created by wangxiang on 2021/12/3
 */

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
public class DevopsPipelineTemplateVO {

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

    @ApiModelProperty("模板适用语言")
    private Long ciTemplateLanguageId;

    @ApiModelProperty("是否启用")
    private Boolean enable;

    @ApiModelProperty("版本命令规则")
    private String versionName;

    @ApiModelProperty("镜像地址")
    private String image;

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

    public Long getCiTemplateLanguageId() {
        return ciTemplateLanguageId;
    }

    public void setCiTemplateLanguageId(Long ciTemplateLanguageId) {
        this.ciTemplateLanguageId = ciTemplateLanguageId;
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
