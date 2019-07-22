package io.choerodon.devops.infra.dto.gitlab;

import java.util.Date;
import java.util.List;

import io.choerodon.devops.api.vo.iam.entity.gitlab.MilestoneE;
import io.choerodon.devops.api.vo.iam.entity.gitlab.UserE;

/**
 * GitLab 合并请求
 */
public class MergeRequestDTO {

    private Integer approvalsBeforeMerge;
    private AssigneeDO assignee;
    private AuthorDO author;
    private DiffDTO changes;
    private Date createdAt;
    private String description;
    private Integer downvotes;
    private Boolean forceRemoveSourceBranch;
    private Integer id;
    private Integer iid;
    private List<String> labels;
    private String mergeCommitSha;
    private String mergeStatus;
    private Boolean mergeWhenBuildSucceeds;
    private MilestoneE milestone;
    private Integer projectId;
    private String sha;
    private Boolean shouldRemoveSourceBranch;
    private String sourceBranch;
    private Integer sourceProjectId;
    private Boolean squash;
    private String state;
    private Boolean subscribed;
    private String targetBranch;
    private Integer targetProjectId;
    private String title;
    private Date updatedAt;
    private Integer upvotes;
    private Integer userNotesCount;
    private String webUrl;
    private Boolean workInProgress;

    // The approval fields will only be available when listing approvals, approving  or unapproving a merge reuest.
    private Integer approvalsRequired;
    private Integer approvalsMissing;

    private List<UserE> approvedBy;

    private List<CommitDTO> commits;

    public static Boolean isValid(MergeRequestDTO mergeRequest) {
        return (mergeRequest != null && mergeRequest.getId() != null);
    }

    public List<CommitDTO> getCommits() {
        return commits;
    }

    public void setCommits(List<CommitDTO> commits) {
        this.commits = commits;
    }

    public Integer getApprovalsBeforeMerge() {
        return approvalsBeforeMerge;
    }

    public void setApprovalsBeforeMerge(Integer approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
    }

    public AssigneeDO getAssignee() {
        return assignee;
    }

    public void setAssignee(AssigneeDO assignee) {
        this.assignee = assignee;
    }

    public AuthorDO getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDO author) {
        this.author = author;
    }

    public DiffDTO getChanges() {
        return changes;
    }

    public void setChanges(DiffDTO changes) {
        this.changes = changes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    public Boolean getForceRemoveSourceBranch() {
        return forceRemoveSourceBranch;
    }

    public void setForceRemoveSourceBranch(Boolean forceRemoveSourceBranch) {
        this.forceRemoveSourceBranch = forceRemoveSourceBranch;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIid() {
        return iid;
    }

    public void setIid(Integer iid) {
        this.iid = iid;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getMergeCommitSha() {
        return mergeCommitSha;
    }

    public void setMergeCommitSha(String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }

    public String getMergeStatus() {
        return mergeStatus;
    }

    public void setMergeStatus(String mergeStatus) {
        this.mergeStatus = mergeStatus;
    }

    public Boolean getMergeWhenBuildSucceeds() {
        return mergeWhenBuildSucceeds;
    }

    public void setMergeWhenBuildSucceeds(Boolean mergeWhenBuildSucceeds) {
        this.mergeWhenBuildSucceeds = mergeWhenBuildSucceeds;
    }

    public MilestoneE getMilestone() {
        return milestone;
    }

    public void setMilestone(MilestoneE milestoneE) {
        this.milestone = milestoneE;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Boolean getShouldRemoveSourceBranch() {
        return shouldRemoveSourceBranch;
    }

    public void setShouldRemoveSourceBranch(Boolean shouldRemoveSourceBranch) {
        this.shouldRemoveSourceBranch = shouldRemoveSourceBranch;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public Integer getSourceProjectId() {
        return sourceProjectId;
    }

    public void setSourceProjectId(Integer sourceProjectId) {
        this.sourceProjectId = sourceProjectId;
    }

    public Boolean getSquash() {
        return squash;
    }

    public void setSquash(Boolean squash) {
        this.squash = squash;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public Integer getTargetProjectId() {
        return targetProjectId;
    }

    public void setTargetProjectId(Integer targetProjectId) {
        this.targetProjectId = targetProjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    public Integer getUserNotesCount() {
        return userNotesCount;
    }

    public void setUserNotesCount(Integer userNotesCount) {
        this.userNotesCount = userNotesCount;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public Boolean getWorkInProgress() {
        return workInProgress;
    }

    public void setWorkInProgress(Boolean workInProgress) {
        this.workInProgress = workInProgress;
    }

    /**
     * Get the number of approvals required for the merge request.
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @return the number of approvals required for the merge request
     */
    public Integer getApprovalsRequired() {
        return approvalsRequired;
    }

    /**
     * Set the number of approvals required for the merge request.
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @param approvalsRequired the number of approvals required for the merge request
     */
    public void setApprovalsRequired(Integer approvalsRequired) {
        this.approvalsRequired = approvalsRequired;
    }

    /**
     * Get the number of approvals missing for the merge request.
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @return the number of approvals missing for the merge request
     */
    public Integer getApprovalsMissing() {
        return approvalsMissing;
    }

    /**
     * Set the number of approvals missing for the merge request.
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @param approvalsMissing the number of approvals missing for the merge request
     */
    public void setApprovalsMissing(Integer approvalsMissing) {
        this.approvalsMissing = approvalsMissing;
    }

    /**
     * Get the baseList of users that have approved the merge request.
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @return the baseList of users that have approved the merge request
     */
    public List<UserE> getApprovedBy() {
        return approvedBy;
    }

    /**
     * Set the baseList of users that have approved the merge request.
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @param approvedBy the baseList of users that have approved the merge request
     */
    public void setApprovedBy(List<UserE> approvedBy) {
        this.approvedBy = approvedBy;
    }
}