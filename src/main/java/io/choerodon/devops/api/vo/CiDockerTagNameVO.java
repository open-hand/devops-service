package io.choerodon.devops.api.vo;

import javax.annotation.Nullable;

/**
 * @author scp
 * @date 2020/8/27
 * @description
 */
public class CiDockerTagNameVO {
    private Long dockerJobId;
    private String dockerJobName;
    private String dockerTagName;

    public CiDockerTagNameVO() {
    }

    public CiDockerTagNameVO(@Nullable Long dockerJobId, @Nullable String dockerJobName, @Nullable String dockerTagName) {
        this.dockerJobId = dockerJobId;
        this.dockerJobName = dockerJobName;
        this.dockerTagName = dockerTagName;
    }

    public Long getDockerJobId() {
        return dockerJobId;
    }

    public void setDockerJobId(Long dockerJobId) {
        this.dockerJobId = dockerJobId;
    }

    public String getDockerTagName() {
        return dockerTagName;
    }

    public void setDockerTagName(String dockerTagName) {
        this.dockerTagName = dockerTagName;
    }

    public String getDockerJobName() {
        return dockerJobName;
    }

    public void setDockerJobName(String dockerJobName) {
        this.dockerJobName = dockerJobName;
    }
}
