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
 * 应用版本表(AppServiceImageVersion)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2022-07-13 16:47:42
 */

@ApiModel("应用版本表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_app_service_image_version")
public class AppServiceImageVersionDTO extends AuditDomain {
    private static final long serialVersionUID = 311223429472258678L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_SERVICE_VERSION_ID = "appServiceVersionId";
    public static final String FIELD_HARBOR_REPO_TYPE = "harborRepoType";
    public static final String FIELD_HARBOR_CONFIG_ID = "harborConfigId";
    public static final String FIELD_IMAGE = "image";

    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "应用服务版本，devops_app_service_version.id", required = true)
    @NotNull
    private Long appServiceVersionId;

    @ApiModelProperty(value = "仓库类型(DEFAULT_REPO、CUSTOM_REPO)", required = true)
    @NotBlank
    private String harborRepoType;

    @ApiModelProperty(value = "配置Id", required = true)
    @NotNull
    private Long harborConfigId;

    @ApiModelProperty(value = "镜像名", required = true)
    @NotBlank
    private String image;


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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}

