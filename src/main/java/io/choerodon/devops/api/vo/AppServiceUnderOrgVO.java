package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class AppServiceUnderOrgVO {
    @ApiModelProperty(name = "项目id")
    private Long projectId;
    @ApiModelProperty(name = "项目名称")
    private String projectName;
    @ApiModelProperty(name = "应用列表")
    private List<AppServiceVO> appServices;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<AppServiceVO> getAppServices() {
        return appServices;
    }

    public void setAppServices(List<AppServiceVO> appServices) {
        this.appServices = appServices;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}