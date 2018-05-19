package io.choerodon.devops.infra.dataobject.gitlab;

import io.choerodon.devops.domain.application.entity.gitlab.CommitE;


public class BranchDO {
    private CommitE commit;
    private Boolean developersCanMerge;
    private Boolean developersCanPush;
    private Boolean merged;
    private String name;
    private Boolean isProtected;

    public static Boolean isValid(BranchDO branch) {
        return (branch != null && branch.getName() != null);
    }

    public CommitE getCommit() {
        return commit;
    }

    public void setCommit(CommitE commit) {
        this.commit = commit;
    }

    public Boolean getDevelopersCanMerge() {
        return developersCanMerge;
    }

    public void setDevelopersCanMerge(Boolean developersCanMerge) {
        this.developersCanMerge = developersCanMerge;
    }

    public Boolean getDevelopersCanPush() {
        return developersCanPush;
    }

    public void setDevelopersCanPush(Boolean developersCanPush) {
        this.developersCanPush = developersCanPush;
    }

    public Boolean getMerged() {
        return merged;
    }

    public void setMerged(Boolean merged) {
        this.merged = merged;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getProtected() {
        return isProtected;
    }

    public void setProtected(Boolean isProtected) {
        this.isProtected = isProtected;
    }
}