package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import io.choerodon.mybatis.entity.BaseDTO;


/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 14:23
 * Description:
 */

@Table(name = "devops_merge_request")
public class DevopsMergeRequestDO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private Long gitlabMergeRequestId;

    private Long authorId;

    private Long assigneeId;

    private String sourceBranch;

    private String targetBranch;

    private String state;

    private String title;

    private Date createdAt;

    private Date updatedAt;

    public DevopsMergeRequestDO() {
    }

    /**
     * constructor a new merge request item
     *
     * @param projectId    devops application ID
     * @param sourceBranch source branch to merge
     * @param targetBranch target merge branch
     */
    public DevopsMergeRequestDO(Long projectId, String sourceBranch, String targetBranch) {
        this.projectId = projectId;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getGitlabMergeRequestId() {
        return gitlabMergeRequestId;
    }

    public void setGitlabMergeRequestId(Long gitlabMergeRequestId) {
        this.gitlabMergeRequestId = gitlabMergeRequestId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
