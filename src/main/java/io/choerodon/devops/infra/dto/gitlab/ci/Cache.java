package io.choerodon.devops.infra.dto.gitlab.ci;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-2
 */
public class Cache {
    @ApiModelProperty("Cache key used to define a cache affinity.")
    private Key key;
    @ApiModelProperty("Specify which paths should be cached across builds.")
    private List<String> paths;
    @ApiModelProperty("Set untracked: true to cache all files that are untracked in your Git repository")
    private Boolean untracked;
    /**
     * 可选值参考 {@link CachePolicy}
     */
    @ApiModelProperty("should be pull-push, push, or pull")
    private String policy;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public Boolean getUntracked() {
        return untracked;
    }

    public void setUntracked(Boolean untracked) {
        this.untracked = untracked;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
