package io.choerodon.devops.infra.dto.gitlab;

import java.util.List;

/**
 * User: Mr.Wang
 * Date: 2020/2/13
 */
public class GitlabTransferDTO {
    /**
     * 分支名称
     */
    private String branchName;
    /**
     * 创建时间
     */
    private String since;

    /**
     * 要创建的分支名称
     */
    private String sourceBranch;

    /**
     * 源分支名称
     */
    private String targetBranch;

    /**
     * title
     */
    private String title;

    /**
     * description
     */
    private String description;

    /**
     * key
     */
    private String key;
    /**
     * value
     */
    private String value;

    /**
     * merge权限
     */
    private String mergeAccessLevel;

    /**
     * push权限
     */
    private String pushAccessLevel;
    /**
     * key（SSH key）
     */
    private String sshKey;

    /**
     * 标签名
     */
    private String tagName;
    /**
     * 标签源
     */
    private String ref;

    /**
     * 标签描述
     */
    private String msg;

    /**
     * 发布日志
     */
    private String releaseNotes;

    /**
     * from
     */
    private String from;

    /**
     * to
     */
    private String to;

    /**
     * 创建user
     * @return
     */
    private GitlabUserReqDTO gitlabUserReqDTO;

    /**
     * MR相关参数
     */
    private String password;
    private Integer assigneeId;
    private List<Integer> assigneeIds;
    private Integer milestoneId;
    private List<String> labels;
    private Integer targetProjectId;
    private Boolean removeSourceBranch;
    private Boolean squash;
    private Boolean discussionLocked;
    private Boolean allowCollaboration;
    private Integer approvalsBeforeMerge;


    public Integer getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
    }

    public List<Integer> getAssigneeIds() {
        return assigneeIds;
    }

    public void setAssigneeIds(List<Integer> assigneeIds) {
        this.assigneeIds = assigneeIds;
    }

    public Integer getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(Integer milestoneId) {
        this.milestoneId = milestoneId;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Integer getTargetProjectId() {
        return targetProjectId;
    }

    public void setTargetProjectId(Integer targetProjectId) {
        this.targetProjectId = targetProjectId;
    }

    public Boolean getSquash() {
        return squash;
    }

    public void setSquash(Boolean squash) {
        this.squash = squash;
    }

    public Boolean getDiscussionLocked() {
        return discussionLocked;
    }

    public void setDiscussionLocked(Boolean discussionLocked) {
        this.discussionLocked = discussionLocked;
    }

    public Boolean getAllowCollaboration() {
        return allowCollaboration;
    }

    public void setAllowCollaboration(Boolean allowCollaboration) {
        this.allowCollaboration = allowCollaboration;
    }

    public Integer getApprovalsBeforeMerge() {
        return approvalsBeforeMerge;
    }

    public void setApprovalsBeforeMerge(Integer approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
    }

    public Boolean getRemoveSourceBranch() {
        return removeSourceBranch;
    }

    public void setRemoveSourceBranch(Boolean removeSourceBranch) {
        this.removeSourceBranch = removeSourceBranch;
    }

    public GitlabUserReqDTO getGitlabUserReqDTO() {
        return gitlabUserReqDTO;
    }

    public void setGitlabUserReqDTO(GitlabUserReqDTO gitlabUserReqDTO) {
        this.gitlabUserReqDTO = gitlabUserReqDTO;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMergeAccessLevel() {
        return mergeAccessLevel;
    }

    public void setMergeAccessLevel(String mergeAccessLevel) {
        this.mergeAccessLevel = mergeAccessLevel;
    }

    public String getPushAccessLevel() {
        return pushAccessLevel;
    }

    public void setPushAccessLevel(String pushAccessLevel) {
        this.pushAccessLevel = pushAccessLevel;
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
