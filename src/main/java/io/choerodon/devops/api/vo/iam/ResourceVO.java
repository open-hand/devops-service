package io.choerodon.devops.api.vo.iam;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/5/7
 * @Modified By:
 */
public class ResourceVO {
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("总应用服务数")
    private Long currentAppService;
    @ApiModelProperty("总集群数")
    private Long currentCluster;
    @ApiModelProperty("总环境数")
    private Long currentEnv;

    @ApiModelProperty("当前gitlab的使用量")
    private String currentGitlabCapacity;

    public String getCurrentGitlabCapacity() {
        return currentGitlabCapacity;
    }

    public void setCurrentGitlabCapacity(String currentGitlabCapacity) {
        this.currentGitlabCapacity = currentGitlabCapacity;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getCurrentAppService() {
        return currentAppService;
    }

    public void setCurrentAppService(Long currentAppService) {
        this.currentAppService = currentAppService;
    }

    public Long getCurrentCluster() {
        return currentCluster;
    }

    public void setCurrentCluster(Long currentCluster) {
        this.currentCluster = currentCluster;
    }

    public Long getCurrentEnv() {
        return currentEnv;
    }

    public void setCurrentEnv(Long currentEnv) {
        this.currentEnv = currentEnv;
    }
}
