package io.choerodon.devops.api.vo.appversion;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author hao.wang@zknow.com
 * @since 2022-07-14 10:41:40
 */
public class AppServiceHelmVersionVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "应用服务版本，devops_app_service_version.id", required = true)
    @Encrypt
    private Long appServiceVersionId;
    @ApiModelProperty(value = "配置Id", required = true)
    @Encrypt
    private Long helmConfigId;
    @ApiModelProperty(value = "仓库类型(DEFAULT_REPO、CUSTOM_REPO)", required = true)
    private String harborRepoType;
    @ApiModelProperty(value = "配置Id", required = true)
    @Encrypt
    private Long harborConfigId;
    @ApiModelProperty(value = "参数 ID", required = true)
    @Encrypt
    private Long valueId;
    @ApiModelProperty(value = "readme value id", required = true)
    @Encrypt
    private Long readmeValueId;
    @ApiModelProperty(value = "镜像名", required = true)
    private String image;
    @ApiModelProperty(value = "仓库地址", required = true)
    private String repository;
    @ApiModelProperty(value = "chart包名", required = true)
    private String chartName;
    @ApiModelProperty("版本号")
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

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
