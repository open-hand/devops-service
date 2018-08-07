package io.choerodon.devops.domain.application.valueobject;

import java.util.List;


public class GitOpsSync {
    private List<String> resourceIDs;
    private String type;
    private GitOpsMetaData metadata;

    public List<String> getResourceIDs() {
        return resourceIDs;
    }

    public void setResourceIDs(List<String> resourceIDs) {
        this.resourceIDs = resourceIDs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GitOpsMetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(GitOpsMetaData metadata) {
        this.metadata = metadata;
    }
}
