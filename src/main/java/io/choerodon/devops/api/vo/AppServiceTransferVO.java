package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/18 10:29
 */
public class AppServiceTransferVO {
    @ApiModelProperty("应用服务名称")
    private String name;

    @ApiModelProperty("应用服务code")
    private String code;

    @ApiModelProperty("应用服务的类型")
    private String type;

    @ApiModelProperty("gitlabProjectId")
    private Integer gitlabProjectId;

    @ApiModelProperty("gitlabGroupId")
    private Integer gitlabGroupId;

    @ApiModelProperty("项目id")
    private Long projectId;

    @ApiModelProperty("应用服务id")
    private Long appServiceId;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Integer getGitlabGroupId() {
        return gitlabGroupId;
    }

    public void setGitlabGroupId(Integer gitlabGroupId) {
        this.gitlabGroupId = gitlabGroupId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Integer gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }
}
