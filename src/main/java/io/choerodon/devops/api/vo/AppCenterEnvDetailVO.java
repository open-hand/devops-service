package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
public class AppCenterEnvDetailVO {
    @Encrypt
    @ApiModelProperty("应用中心 应用id")
    private Long appCenterId;
    @ApiModelProperty("环境code")
    private String envCode;
    @ApiModelProperty("环境名称")
    private String envName;
    @ApiModelProperty("部署方式")
    private String deployWay;
    @ApiModelProperty("部署对象")
    private String deployObject;
    @ApiModelProperty("chart来源")
    private String chartSource;
    @ApiModelProperty("chart来源")
    private String chartSourceValue;
    @ApiModelProperty("chart来源，应用服务名称")
    private String appServiceName;
    @ApiModelProperty("chart来源，应用服务code")
    private String appServiceCode;
    @ApiModelProperty("chart状态")
    private String objectStatus;

    @ApiModelProperty("创建者 Id")
    private Long userId;
    @ApiModelProperty("登录名")
    private String loginName;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("头像")
    private String imageUrl;

    @ApiModelProperty("创建时间")
    private Date creationDate;

    public Long getAppCenterId() {
        return appCenterId;
    }

    public void setAppCenterId(Long appCenterId) {
        this.appCenterId = appCenterId;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getDeployWay() {
        return deployWay;
    }

    public void setDeployWay(String deployWay) {
        this.deployWay = deployWay;
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

    public String getObjectStatus() {
        return objectStatus;
    }

    public void setObjectStatus(String objectStatus) {
        this.objectStatus = objectStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getChartSource() {
        return chartSource;
    }

    public void setChartSource(String chartSource) {
        this.chartSource = chartSource;
    }

    public String getChartSourceValue() {
        return chartSourceValue;
    }

    public void setChartSourceValue(String chartSourceValue) {
        this.chartSourceValue = chartSourceValue;
    }

    public String getDeployObject() {
        return deployObject;
    }

    public void setDeployObject(String deployObject) {
        this.deployObject = deployObject;
    }
}
