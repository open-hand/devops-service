package io.choerodon.devops.api.vo;

import java.util.List;

public class TagHookDTO {
    private String checkoutSha;
    private String ref;
    private Long projectId;
    private Long userId;
    private List<CommitVO> commits;

    public String getCheckoutSha() {
        return checkoutSha;
    }

    public void setCheckoutSha(String checkoutSha) {
        this.checkoutSha = checkoutSha;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CommitVO> getCommits() {
        return commits;
    }

    public void setCommits(List<CommitVO> commits) {
        this.commits = commits;
    }
}
