package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/9
 * @Modified By:
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_app_template")
public class DevopsAppTemplateDTO  extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Encrypt
    private Long id;

    private Long sourceId;
    private String sourceType;
    private Long gitlabProjectId;
    private String gitlabUrl;
    private String name;
    private String code;
    private String type;
    private Boolean enable;
    private String status;
    @Transient
    private Boolean permission;

    public DevopsAppTemplateDTO() {
    }

    public DevopsAppTemplateDTO(Long id, Long sourceId, String sourceType) {
        this.id = id;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
    }

    public DevopsAppTemplateDTO(Long sourceId, String sourceType,  String code) {
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }
}
