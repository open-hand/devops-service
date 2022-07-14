package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.appversion.AppServiceHelmVersionVO;
import io.choerodon.devops.api.vo.appversion.AppServiceImageVersionVO;
import io.choerodon.devops.api.vo.appversion.AppServiceMavenVersionVO;

/**
 * Created by younger on 2018/4/14.
 */
public class AppServiceVersionVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("版本号")
    private String version;
    @Encrypt
    @ApiModelProperty("版本管理应用服务id")
    private Long appServiceId;
    @ApiModelProperty("版本创建时间")
    private Date creationDate;
    @ApiModelProperty("版本中镜像推送的镜像仓库类型")
    private String repoType;
    @ApiModelProperty("版本是否可以删除标记")
    private Boolean deleteFlag = true;
    @ApiModelProperty("版本最近更新时间")
    private Date lastUpdateDate;

    @ApiModelProperty("docker镜像版本")
    private String image;
    @ApiModelProperty(value = "关联的helm版本信息")
    private AppServiceHelmVersionVO appServiceHelmVersionVO;
    @ApiModelProperty(value = "关联的maven版本信息")
    private AppServiceMavenVersionVO appServiceMavenVersionVO;
    @ApiModelProperty(value = "关联的镜像版本信息")
    private AppServiceImageVersionVO appServiceImageVersionVO;

    public AppServiceHelmVersionVO getAppServiceHelmVersionVO() {
        return appServiceHelmVersionVO;
    }

    public void setAppServiceHelmVersionVO(AppServiceHelmVersionVO appServiceHelmVersionVO) {
        this.appServiceHelmVersionVO = appServiceHelmVersionVO;
    }

    public AppServiceMavenVersionVO getAppServiceMavenVersionVO() {
        return appServiceMavenVersionVO;
    }

    public void setAppServiceMavenVersionVO(AppServiceMavenVersionVO appServiceMavenVersionVO) {
        this.appServiceMavenVersionVO = appServiceMavenVersionVO;
    }

    public AppServiceImageVersionVO getAppServiceImageVersionVO() {
        return appServiceImageVersionVO;
    }

    public void setAppServiceImageVersionVO(AppServiceImageVersionVO appServiceImageVersionVO) {
        this.appServiceImageVersionVO = appServiceImageVersionVO;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
