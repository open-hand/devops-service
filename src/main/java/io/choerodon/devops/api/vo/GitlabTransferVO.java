package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotEmpty;

/**
 * User: Mr.Wang
 * Date: 2020/2/13
 */
public class GitlabTransferVO {
    /**
     * 分支名称
     */
    @NotEmpty(message = "分支名称不能为null", groups = {GetCommits.class, AddProtectedBranches.class, CreateBranch.class})
    private String branchName;
    /**
     * 创建时间
     */
    @NotEmpty(message = "创建时间不能为null", groups = {GetCommits.class})
    private String since;

    public interface GetCommits {
    }

    /**
     * 要创建的分支名称
     */
    @NotEmpty(message = "要创建的分支名称不能为null", groups = {CreateMerge.class, CreateBranch.class})
    private String sourceBranch;

    /**
     * 源分支名称
     */
    @NotEmpty(message = "源分支名称不能为null", groups = CreateMerge.class)
    private String targetBranch;

    /**
     * title
     */
    @NotEmpty(message = "title不能为null", groups = {CreateMerge.class, DeployKey.class})
    private String title;

    /**
     * description
     */
    @NotEmpty(message = "description不能为null", groups = CreateMerge.class)
    private String description;

    public interface CreateMerge {
    }

    /**
     * key
     */
    @NotEmpty(message = "key不能为null", groups = AddCiProjectVariable.class)
    private String key;
    /**
     * value
     */
    @NotEmpty(message = "value不能为null", groups = AddCiProjectVariable.class)
    private String value;

    public interface AddCiProjectVariable {
    }

    /**
     * merge权限
     */
    @NotEmpty(message = "merge权限不能为null", groups = AddProtectedBranches.class)
    private String mergeAccessLevel;

    /**
     * push权限
     */
    @NotEmpty(message = "push权限不能为null", groups = AddProtectedBranches.class)
    private String pushAccessLevel;

    public interface AddProtectedBranches {
    }

    /**
     * key（SSH key）
     */
    private String sshKey;

    public interface DeployKey {
    }

    //创建新分支
    public interface CreateBranch {
    }

    /**
     * 标签名
     */
    @NotEmpty(message = "标签名不能为null", groups = {CreateTag.class, UpdateTag.class})
    private String tagName;
    /**
     * 标签源
     */
    @NotEmpty(message = "标签源不能为null", groups = CreateTag.class)
    private String ref;

    /**
     * 标签描述
     */
    private String msg;

    /**
     * 发布日志
     */
    private String releaseNotes;

    public interface CreateTag {
    }

    public interface UpdateTag {
    }

    /**
     * from
     */
    @NotEmpty(message = "from不能为null", groups = GetDiffs.class)
    private String from;

    /**
     * to
     */
    @NotEmpty(message = "to不能为null", groups = GetDiffs.class)
    private String to;

    public interface GetDiffs {
    }


    /**
     * 创建user
     *
     * @return
     */
    private GitlabUserReqDTO gitlabUserReqDTO;
    private String password;

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
