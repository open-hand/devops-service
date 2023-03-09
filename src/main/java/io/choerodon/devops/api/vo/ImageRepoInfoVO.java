package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/14 15:02
 */
public class ImageRepoInfoVO {
    private String harborRepoId;
    private String repoType;

    private String repoCode;
    private String dockerRegistry;
    private String groupName;

    private String dockerUsername;
    private String dockerPassword;

    public ImageRepoInfoVO() {

    }

    public ImageRepoInfoVO(String harborRepoId, String repoType, String dockerRegistry, String groupName) {
        this.harborRepoId = harborRepoId;
        this.repoType = repoType;
        this.dockerRegistry = dockerRegistry;
        this.groupName = groupName;
    }

    public String getDockerUsername() {
        return dockerUsername;
    }

    public void setDockerUsername(String dockerUsername) {
        this.dockerUsername = dockerUsername;
    }

    public String getDockerPassword() {
        return dockerPassword;
    }

    public void setDockerPassword(String dockerPassword) {
        this.dockerPassword = dockerPassword;
    }

    public String getHarborRepoId() {
        return harborRepoId;
    }

    public void setHarborRepoId(String harborRepoId) {
        this.harborRepoId = harborRepoId;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public String getDockerRegistry() {
        return dockerRegistry;
    }

    public void setDockerRegistry(String dockerRegistry) {
        this.dockerRegistry = dockerRegistry;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRepoCode() {
        return repoCode;
    }

    public void setRepoCode(String repoCode) {
        this.repoCode = repoCode;
    }
}
