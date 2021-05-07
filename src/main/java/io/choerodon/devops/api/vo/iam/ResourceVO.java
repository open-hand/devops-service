package io.choerodon.devops.api.vo.iam;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/5/7
 * @Modified By:
 */
public class ResourceVO {
    private Long projectId;
    private Long clusterNum;
    private Long envNum;
    private Long appServiceNum;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getClusterNum() {
        return clusterNum;
    }

    public void setClusterNum(Long clusterNum) {
        this.clusterNum = clusterNum;
    }

    public Long getEnvNum() {
        return envNum;
    }

    public void setEnvNum(Long envNum) {
        this.envNum = envNum;
    }

    public Long getAppServiceNum() {
        return appServiceNum;
    }

    public void setAppServiceNum(Long appServiceNum) {
        this.appServiceNum = appServiceNum;
    }
}
