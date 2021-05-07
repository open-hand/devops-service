package io.choerodon.devops.api.vo.iam;

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
