package io.choerodon.devops.infra.dto;


import javax.persistence.*;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by Zenger on 2018/4/3.
 */
@ModifyAudit
@VersionAudit
@ApiModel("应用服务版本DTO")
@Table(name = "devops_app_service_version")
public class AppServiceVersionDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("版本号")
    private String version;
    @Encrypt
    @ApiModelProperty("版本关联的应用服务id")
    private Long appServiceId;
    @ApiModelProperty("版本关联的Gitlab commit sha")
    private String commit;
    @ApiModelProperty("版本关联的分支")
    private String ref;

    @Transient
    @ApiModelProperty("版本关联的应用服务名称")
    private String appServiceName;
    @Transient
    @ApiModelProperty("版本关联的应用服务编码")
    private String appServiceCode;
    @Transient
    @ApiModelProperty("版本关联的应用服务状态")
    private Boolean appServiceStatus;
    @Transient
    @ApiModelProperty("版本关联的应用服务类型")
    private String appServiceType;
    @Transient
    @ApiModelProperty("版本的readme内容")
    private String readme;
    @Transient
    @ApiModelProperty("版本的values内容")
    private String values;
    @Transient
    @ApiModelProperty("版本的chart名称")
    private String chartName;
    @Transient
    @ApiModelProperty("版本的所属的项目id")
    private Long projectId;


    @Encrypt
    @Transient
    @ApiModelProperty("版本关联的valuesId")
    private Long valueId;
    @Encrypt
    @Transient
    @ApiModelProperty("版本关联的READMEId")
    private Long readmeValueId;
    @ApiModelProperty("版本关联的harbor镜像地址")
    @Transient
    private String image;
    @ApiModelProperty("版本关联的chart仓库地址")
    @Transient
    private String repository;
    @Encrypt
    @ApiModelProperty("版本关联的harbor仓库id")
    @Transient
    private Long harborConfigId;
    @Encrypt
    @ApiModelProperty("版本关联的helm仓库id")
    @Transient
    private Long helmConfigId;
    @ApiModelProperty("版本关联的harbor仓库类型")
    @Transient
    private String repoType;


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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
