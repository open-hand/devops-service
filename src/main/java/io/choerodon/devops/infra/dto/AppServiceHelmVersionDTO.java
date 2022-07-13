package io.choerodon.devops.infra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 应用版本表(AppServiceHelmVersion)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:41
 */

@ApiModel("应用版本表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_app_service_helm_version")
public class AppServiceHelmVersionDTO extends AuditDomain {
    private static final long serialVersionUID = 769273160524234032L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_SERVICE_VERSION_ID = "appServiceVersionId";
    public static final String FIELD_HELM_CONFIG_ID = "helmConfigId";
    public static final String FIELD_HARBOR_REPO_TYPE = "harborRepoType";
    public static final String FIELD_HARBOR_CONFIG_ID = "harborConfigId";
    public static final String FIELD_VALUE_ID = "valueId";
    public static final String FIELD_README_VALUE_ID = "readmeValueId";
    public static final String FIELD_IMAGE = "image";
    public static final String FIELD_REPOSITORY = "repository";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "应用服务版本，devops_app_service_version.id", required = true)
    @NotNull
    private Long appServiceVersionId;

    @ApiModelProperty(value = "配置Id", required = true)
    @NotNull
    private Long helmConfigId;

    @ApiModelProperty(value = "仓库类型(DEFAULT_REPO、CUSTOM_REPO)", required = true)
    @NotBlank
    private String harborRepoType;

    @ApiModelProperty(value = "配置Id", required = true)
    @NotNull
    private Long harborConfigId;

    @ApiModelProperty(value = "参数 ID", required = true)
    @NotNull
    private Long valueId;

    @ApiModelProperty(value = "readme value id", required = true)
    @NotNull
    private Long readmeValueId;

    @ApiModelProperty(value = "镜像名", required = true)
    @NotBlank
    private String image;

    @ApiModelProperty(value = "仓库地址", required = true)
    @NotBlank
    private String repository;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppServiceVersionId() {
        return appServiceVersionId;
    }

    public void setAppServiceVersionId(Long appServiceVersionId) {
        this.appServiceVersionId = appServiceVersionId;
    }

    public Long getHelmConfigId() {
        return helmConfigId;
    }

    public void setHelmConfigId(Long helmConfigId) {
        this.helmConfigId = helmConfigId;
    }

    public String getHarborRepoType() {
        return harborRepoType;
    }

    public void setHarborRepoType(String harborRepoType) {
        this.harborRepoType = harborRepoType;
    }

    public Long getHarborConfigId() {
        return harborConfigId;
    }

    public void setHarborConfigId(Long harborConfigId) {
        this.harborConfigId = harborConfigId;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

}

