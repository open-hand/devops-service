package io.choerodon.devops.infra.dto.gitlab;


public class BranchDTO {
    private CommitDTO commit;
    private Boolean developersCanMerge;
    private Boolean developersCanPush;
    private Boolean merged;
    private String name;
    private Boolean isProtected;

    public static Boolean isValid(BranchDTO branch) {
        return (branch != null && branch.getName() != null);
    }

    public CommitDTO getCommit() {
        return commit;
    }

    public void setCommit(CommitDTO commit) {
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