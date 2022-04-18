package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/14 15:02
 */
public class ImageRepoInfoVO {
    private Long harborRepoId;
    private String repoType;
    private String dockerRegistry;
    private String groupName;

    public ImageRepoInfoVO() {

    }

    public ImageRepoInfoVO(Long harborRepoId, String repoType, String dockerRegistry, String groupName) {
        this.harborRepoId = harborRepoId;
        this.repoType = repoType;
        this.dockerRegistry = dockerRegistry;
        this.groupName = groupName;
    }

    public Long getHarborRepoId() {
        return harborRepoId;
    }

    public void setHarborRepoId(Long harborRepoId) {
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
}
