package io.choerodon.devops.infra.dataobject;

import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by ernst on 2018/5/12.
 */
@VersionAudit
@ModifyAudit
@Table(name = "devops_app_market")
public class DevopsAppMarketDO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    private Long appId;
    private String contributor;
    private String description;
    private String imgUrl;
    private String publishLevel;

    @Transient
    private String name;
    @Transient
    private String code;
    @Transient
    private Long organizationId;
    @Transient
    private List<ApplicationVersionDO> applicationVersionDOList;

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

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPublishLevel() {
        return publishLevel;
    }

    public void setPublishLevel(String publishLevel) {
        this.publishLevel = publishLevel;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public List<ApplicationVersionDO> getApplicationVersionDOList() {
        return applicationVersionDOList;
    }

    public void setApplicationVersionDOList(List<ApplicationVersionDO> applicationVersionDOList) {
        this.applicationVersionDOList = applicationVersionDOList;
    }
}
