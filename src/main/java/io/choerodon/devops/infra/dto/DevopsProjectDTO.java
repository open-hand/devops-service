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
    private Long appId;
    private Long devopsAppGroupId;
    private Long devopsEnvGroupId;
    private Boolean harborProjectIsPrivate;
    private String harborProjectUserName;
    private String harborProjectUserPassword;
    private String harborProjectUserEmail;

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

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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

    public String getHarborProjectUserName() {
        return harborProjectUserName;
    }

    public void setHarborProjectUserName(String harborProjectUserName) {
        this.harborProjectUserName = harborProjectUserName;
    }

    public String getHarborProjectUserPassword() {
        return harborProjectUserPassword;
    }

    public void setHarborProjectUserPassword(String harborProjectUserPassword) {
        this.harborProjectUserPassword = harborProjectUserPassword;
    }

    public String getHarborProjectUserEmail() {
        return harborProjectUserEmail;
    }

    public void setHarborProjectUserEmail(String harborProjectUserEmail) {
        this.harborProjectUserEmail = harborProjectUserEmail;
    }

    public Boolean getHarborProjectIsPrivate() {
        return harborProjectIsPrivate;
    }

    public void setHarborProjectIsPrivate(Boolean harborProjectIsPrivate) {
        this.harborProjectIsPrivate = harborProjectIsPrivate;
    }
}
