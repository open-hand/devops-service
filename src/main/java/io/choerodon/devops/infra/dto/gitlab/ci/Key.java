package io.choerodon.devops.infra.dto.gitlab.ci;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-2
 */
public class Key {
    @ApiModelProperty("Prefix that is added to the final cache key")
    private String prefix;
    @ApiModelProperty("Files that should be used to build the key")
    private List<String> files;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
