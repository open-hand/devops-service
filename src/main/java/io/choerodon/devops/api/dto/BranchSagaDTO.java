package io.choerodon.devops.api.dto;

/**
 * Created by Sheep on 2019/7/4.
 */
public class BranchSagaDTO {

    private Long gitlabProjectId;
    private Long devopsBranchId;
    private String branchName;
    private String originBranch;


    public BranchSagaDTO() {
    }


    public BranchSagaDTO(Long gitlabProjectId, Long devopsBranchId, String branchName, String originBranch) {
        this.gitlabProjectId = gitlabProjectId;
        this.devopsBranchId = devopsBranchId;
        this.branchName = branchName;
        this.originBranch = originBranch;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public Long getDevopsBranchId() {
        return devopsBranchId;
    }

    public void setDevopsBranchId(Long devopsBranchId) {
        this.devopsBranchId = devopsBranchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getOriginBranch() {
        return originBranch;
    }

    public void setOriginBranch(String originBranch) {
        this.originBranch = originBranch;
    }
}
