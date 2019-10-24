package io.choerodon.devops.infra.dto;

import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by younger on 2018/3/29.
 */
@Table(name = "devops_project")
public class DevopsProjectDTO extends BaseDTO {
    @Id
    private Long iamProjectId;
    private Long devopsAppGroupId;
    private Long devopsEnvGroupId;
    private Boolean harborProjectIsPrivate;
    private Long harborPullUserId;
    private Long harborUserId;

    public DevopsProjectDTO() {
    }

    public DevopsProjectDTO(Long iamProjectId) {
        this.iamProjectId = iamProjectId;
    }

    public Long getIamProjectId() {
        return iamProjectId;
    }

    public void setIamProjectId(Long iamProjectId) {
        this.iamProjectId = iamProjectId;
    }

    public Long getDevopsAppGroupId() {
        return devopsAppGroupId;
    }

    public void setDevopsAppGroupId(Long devopsAppGroupId) {
        this.devopsAppGroupId = devopsAppGroupId;
    }

    public Long getDevopsEnvGroupId() {
        return devopsEnvGroupId;
    }

    public void setDevopsEnvGroupId(Long devopsEnvGroupId) {
        this.devopsEnvGroupId = devopsEnvGroupId;
    }

    public Boolean getHarborProjectIsPrivate() {
        return harborProjectIsPrivate;
    }

    public void setHarborProjectIsPrivate(Boolean harborProjectIsPrivate) {
        this.harborProjectIsPrivate = harborProjectIsPrivate;
    }

    public Long getHarborPullUserId() {
        return harborPullUserId;
    }

    public void setHarborPullUserId(Long harborPullUserId) {
        this.harborPullUserId = harborPullUserId;
    }

    public Long getHarborUserId() {
        return harborUserId;
    }

    public void setHarborUserId(Long harborUserId) {
        this.harborUserId = harborUserId;
    }
}
