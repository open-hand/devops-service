package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 14:23
 * Description:
 */
@ModifyAudit
@Table(name = "devops_merge_request")
public class DevopsMergeRequestDO {

    @Id
    @GeneratedValue
    private Long id;

    private Long applicationId;

    private String sourceBranch;

    private String targetBranch;

    private Long mergeRequestId;

    public DevopsMergeRequestDO() {
    }

    /**
     * constructor a new merge request item
     *
     * @param applicationId devops application ID
     * @param sourceBranch  source branch to merge
     * @param targetBranch  target merge branch
     */
    public DevopsMergeRequestDO(Long applicationId, String sourceBranch, String targetBranch) {
        this.applicationId = applicationId;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
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

    public Long getMergeRequestId() {
        return mergeRequestId;
    }

    public void setMergeRequestId(Long mergeRequestId) {
        this.mergeRequestId = mergeRequestId;
    }
}
