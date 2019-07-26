package io.choerodon.devops.infra.dto;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by ernst on 2018/5/12.
 */
@Table(name = "devops_app_share_rule")
public class ApplicationShareRuleDTO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long appId;
    private String shareLevel;
    private String versionType;
    private String version;
    private Long projectId;
    private Long organizationId;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getShareLevel() {
        return shareLevel;
    }

    public void setShareLevel(String shareLevel) {
        this.shareLevel = shareLevel;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
