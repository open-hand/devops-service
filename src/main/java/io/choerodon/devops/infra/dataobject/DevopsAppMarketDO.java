package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    private String category;
    private String imgUrl;
    private String publishLevel;
    private Boolean isActive;

    @Transient
    private String name;
    @Transient
    private String code;
    @Transient
    private Long organizationId;
    @Transient
    private List<ApplicationVersionDO> applicationVersionDOList;
    @Transient
    private Boolean isDeployed;
    @Transient
    private Date appUpdatedDate;
    @Transient
    private Date marketUpdatedDate;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public List<ApplicationVersionDO> getApplicationVersionDOList() {
        return applicationVersionDOList;
    }

    public void setApplicationVersionDOList(List<ApplicationVersionDO> applicationVersionDOList) {
        this.applicationVersionDOList = applicationVersionDOList;
    }

    public Boolean getDeployed() {
        return isDeployed;
    }

    public void setDeployed(Boolean deployed) {
        isDeployed = deployed;
    }

    public Date getAppUpdatedDate() {
        return appUpdatedDate;
    }

    public void setAppUpdatedDate(Date appUpdatedDate) {
        this.appUpdatedDate = appUpdatedDate;
    }

    public Date getMarketUpdatedDate() {
        return marketUpdatedDate;
    }

    public void setMarketUpdatedDate(Date marketUpdatedDate) {
        this.marketUpdatedDate = marketUpdatedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DevopsAppMarketDO that = (DevopsAppMarketDO) o;
        return Objects.equals(id, that.id)
                && Objects.equals(appId, that.appId)
                && Objects.equals(contributor, that.contributor)
                && Objects.equals(description, that.description)
                && Objects.equals(category, that.category)
                && Objects.equals(imgUrl, that.imgUrl)
                && Objects.equals(publishLevel, that.publishLevel)
                && Objects.equals(isActive, that.isActive);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, appId, contributor, description, category, imgUrl, publishLevel, isActive);
    }
}
