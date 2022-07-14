package io.choerodon.devops.api.vo.appversion;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang@zknow.com
 * @since 2022-07-14 10:42:13
 */
public class AppServiceImageVersionVO {


    private Long id;
    @ApiModelProperty(value = "应用服务版本，devops_app_service_version.id", required = true)
    private Long appServiceVersionId;
    @ApiModelProperty(value = "仓库类型(DEFAULT_REPO、CUSTOM_REPO)", required = true)
    private String harborRepoType;
    @ApiModelProperty(value = "配置Id", required = true)
    private Long harborConfigId;
    @ApiModelProperty(value = "镜像名", required = true)
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
