package io.choerodon.devops.infra.dto;


import javax.persistence.*;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by Zenger on 2018/4/3.
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_app_service_version")
public class AppServiceVersionDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String version;
    private Long appServiceId;
    private Long valueId;
    private Long readmeValueId;
    private String image;
    private String commit;
    private String repository;
    private Long harborConfigId;
    private Long helmConfigId;
    private String repoType;
    private String ref;

    @Transient
    private String appServiceName;
    @Transient
    private String appServiceCode;
    @Transient
    private Boolean appServiceStatus;
    @Transient
    private String appServiceType;
    @Transient
    private String readme;
    @Transient
    private String values;
    @Transient
    private String chartName;


    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
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

    public Long getReadmeValueId() {
        return readmeValueId;
    }

    public void setReadmeValueId(Long readmeValueId) {
        this.readmeValueId = readmeValueId;
    }

    public String getAppServiceType() {
        return appServiceType;
    }

    public void setAppServiceType(String appServiceType) {
        this.appServiceType = appServiceType;
    }

    public Long getHarborConfigId() {
        return harborConfigId;
    }

    public void setHarborConfigId(Long harborConfigId) {
        this.harborConfigId = harborConfigId;
    }

    public Long getHelmConfigId() {
        return helmConfigId;
    }

    public void setHelmConfigId(Long helmConfigId) {
        this.helmConfigId = helmConfigId;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
