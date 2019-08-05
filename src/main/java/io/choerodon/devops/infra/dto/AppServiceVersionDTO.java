package io.choerodon.devops.infra.dto;


import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by Zenger on 2018/4/3.
 */

@Table(name = "devops_app_service_version")
public class AppServiceVersionDTO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String version;
    private Long appServiceId;
    private Long valueId;
    private Long readmeValueId;
    private String image;
    private String commit;
    private String repository;
    private Long isPublish;
    private Date publishTime;

    @Transient
    private String appServiceName;
    @Transient
    private String appServiceCode;
    @Transient
    private Boolean appServiceStatus;
    @Transient
    private String readme;

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
    }

    public Boolean getAppServiceStatus() {
        return appServiceStatus;
    }

    public void setAppServiceStatus(Boolean appServiceStatus) {
        this.appServiceStatus = appServiceStatus;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public Long getIsPublish() {
        return isPublish;
    }

    public void setIsPublish(Long isPublish) {
        this.isPublish = isPublish;
    }

    public Long getReadmeValueId() {
        return readmeValueId;
    }

    public void setReadmeValueId(Long readmeValueId) {
        this.readmeValueId = readmeValueId;
    }

}
