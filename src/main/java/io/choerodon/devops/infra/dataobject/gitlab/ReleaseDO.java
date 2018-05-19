package io.choerodon.devops.infra.dataobject.gitlab;

/**
 * GitLab release
 *
 * @author Runge
 */
public class ReleaseDO {

    private String tagName;
    private String description;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}