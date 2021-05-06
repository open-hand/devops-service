package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by ernst on 2018/5/12.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_app_service_share_rule")
public class AppServiceShareRuleDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("共享规则所属的应用服务id")
    private Long appServiceId;
    @ApiModelProperty("共享的层次，组织层 或者 项目层特定项目")
    private String shareLevel;
    @ApiModelProperty("版本类型")
    private String versionType;
    @ApiModelProperty("特定版本")
    private String version;
    @ApiModelProperty("shareLevel为project时，目标项目的id")
    private Long projectId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
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
