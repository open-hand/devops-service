package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/10
 * @Modified By:
 */
public class DevopsAppTemplateCreateVO {
    @ApiModelProperty("应用模板名称")
    private String name;
    @ApiModelProperty("应用模板code")
    private String code;
    @ApiModelProperty("创建方式：template（已有模板创建）;gitlab;github")
    private String createType;
    @ApiModelProperty("被选择的应用模板code")
    private String selectedTemplateCode;
    @ApiModelProperty("gitlab/github url")
    private String repoUrl;
    @ApiModelProperty("私有token")
    private String token;

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

    public String getSelectedTemplateCode() {
        return selectedTemplateCode;
    }

    public void setSelectedTemplateCode(String selectedTemplateCode) {
        this.selectedTemplateCode = selectedTemplateCode;
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
}
