package io.choerodon.devops.api.vo.market;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by wangxiang on 2021/3/2
 * 市场应用源码发布包的对象
 */
public class MarketSourceCodeVO {

    /**
     * 应用服务名字
     */
    private String devopsAppServiceName;

    /**
     * 应用服务版本：2021.2.24-182534-master
     */
    private String devopsAppServiceVersion;

    /**
     * 应用服务版本id
     */
    @Encrypt
    private Long devopsAppServiceVersionId;

    /**
     *  应用服务的id
     */
    @Encrypt
    private Long devopsAppServiceId;
    /**
     * 应用服务code
     */
    private String devopsAppServiceCode;

    /**
     * 源代码的地址
     */
    private String marketSourceCodeUrl;


    public String getDevopsAppServiceName() {
        return devopsAppServiceName;
    }

    public void setDevopsAppServiceName(String devopsAppServiceName) {
        this.devopsAppServiceName = devopsAppServiceName;
    }

    public String getDevopsAppServiceVersion() {
        return devopsAppServiceVersion;
    }

    public void setDevopsAppServiceVersion(String devopsAppServiceVersion) {
        this.devopsAppServiceVersion = devopsAppServiceVersion;
    }

    public Long getDevopsAppServiceVersionId() {
        return devopsAppServiceVersionId;
    }

    public void setDevopsAppServiceVersionId(Long devopsAppServiceVersionId) {
        this.devopsAppServiceVersionId = devopsAppServiceVersionId;
    }

    public Long getDevopsAppServiceId() {
        return devopsAppServiceId;
    }

    public void setDevopsAppServiceId(Long devopsAppServiceId) {
        this.devopsAppServiceId = devopsAppServiceId;
    }

    public String getDevopsAppServiceCode() {
        return devopsAppServiceCode;
    }

    public void setDevopsAppServiceCode(String devopsAppServiceCode) {
        this.devopsAppServiceCode = devopsAppServiceCode;
    }

    public String getMarketSourceCodeUrl() {
        return marketSourceCodeUrl;
    }

    public void setMarketSourceCodeUrl(String marketSourceCodeUrl) {
        this.marketSourceCodeUrl = marketSourceCodeUrl;
    }
}
