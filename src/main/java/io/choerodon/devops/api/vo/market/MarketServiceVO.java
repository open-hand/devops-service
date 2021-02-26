package io.choerodon.devops.api.vo.market;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;


/**
 * Created by wangxiang on 2020/12/16
 */
public class MarketServiceVO {

    private Long id;

    @ApiModelProperty("市场服务名称")
    private String marketServiceName;

    @ApiModelProperty("应用的名称")
    private String marketAppName;

    @Encrypt
    @ApiModelProperty("marketAppId")
    private Long marketAppId;

    @Encrypt
    @ApiModelProperty("应用市场应用版本id")
    private Long marketAppVersionId;

    @ApiModelProperty("市场应用部署对象id")
    private Long marketDeployObjectId;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("错误消息")
    private String errorMessage;

    public String getMarketAppName() {
        return marketAppName;
    }

    public void setMarketAppName(String marketAppName) {
        this.marketAppName = marketAppName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarketServiceName() {
        return marketServiceName;
    }

    public void setMarketServiceName(String marketServiceName) {
        this.marketServiceName = marketServiceName;
    }

    public Long getMarketAppVersionId() {
        return marketAppVersionId;
    }

    public void setMarketAppVersionId(Long marketAppVersionId) {
        this.marketAppVersionId = marketAppVersionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getMarketAppId() {
        return marketAppId;
    }

    public void setMarketAppId(Long marketAppId) {
        this.marketAppId = marketAppId;
    }

    public Long getMarketDeployObjectId() {
        return marketDeployObjectId;
    }

    public void setMarketDeployObjectId(Long marketDeployObjectId) {
        this.marketDeployObjectId = marketDeployObjectId;
    }
}
