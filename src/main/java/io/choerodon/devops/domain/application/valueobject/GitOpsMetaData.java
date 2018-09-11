package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

public class GitOpsMetaData {
    private String commit;
    private List<Error> errors;
    private List<FileCommit> filesCommit;
    private List<ResourceCommit> resourceCommits;

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public List<FileCommit> getFilesCommit() {
        return filesCommit;
    }

    public void setFilesCommit(List<FileCommit> filesCommit) {
        this.filesCommit = filesCommit;
    }

    public List<ResourceCommit> getResourceCommits() {
        return resourceCommits;
    }

    public void setResourceCommits(List<ResourceCommit> resourceCommits) {
        this.resourceCommits = resourceCommits;
    }
}
