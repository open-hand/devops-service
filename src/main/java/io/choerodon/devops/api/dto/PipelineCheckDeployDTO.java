package io.choerodon.devops.api.dto;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:09 2019/5/20
 * Description:
 */
public class PipelineCheckDeployDTO {
    private Boolean permission;
    private Boolean versions;
    private String envName;

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }

    public Boolean getVersions() {
        return versions;
    }

    public void setVersions(Boolean versions) {
        this.versions = versions;
    }
}
