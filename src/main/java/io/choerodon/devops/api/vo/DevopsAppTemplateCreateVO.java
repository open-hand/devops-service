package io.choerodon.devops.api.vo;

import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/10
 * @Modified By:
 */
public class DevopsAppTemplateCreateVO {
    @ApiModelProperty("应用模板名称")
    @Length(min = 1, max = 40, message = "error.app.template.name.invalid")
    private String name;
    @ApiModelProperty("应用模板code")
    @Pattern(regexp = "([a-z]+[a-z0-9-]*[a-z0-9]+){1,30}", message = "error.app.template.code.invalid")
    private String code;
    @ApiModelProperty("创建方式：template（已有模板创建）;gitlab;github")
    private String createType;
    @ApiModelProperty("被选择的应用模板Id")
    @Encrypt
    private Long selectedTemplateId;
    @ApiModelProperty("gitlab/github url")
    private String repoUrl;
    @ApiModelProperty("私有token")
    private String token;
    private Long appTemplateId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCreateType() {
        return createType;
    }

    public void setCreateType(String createType) {
        this.createType = createType;
    }

    public Long getSelectedTemplateId() {
        return selectedTemplateId;
    }

    public void setSelectedTemplateId(Long selectedTemplateId) {
        this.selectedTemplateId = selectedTemplateId;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getAppTemplateId() {
        return appTemplateId;
    }

    public void setAppTemplateId(Long appTemplateId) {
        this.appTemplateId = appTemplateId;
    }
}
