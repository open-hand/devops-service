package io.choerodon.devops.api.vo.iam;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/5/7
 * @Modified By:
 */
public class ResourceVO {
    private Long projectId;
    private Long currentAppService;
    private Long currentCluster;
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
