package io.choerodon.devops.api.vo;

/**
 * Created by wangxiang on 2021/3/3
 */
public class MarketApplicationImportVO {

    /**
     * 服务名称
     */
    private String marketAppServiceName;

    /**
     * 服务编码
     */
    private String marketAppServiceCode;

    /**
     * 源代码在文件服务器上的地址
     */
    private String sourceCodeUrl;

    public String getMarketAppServiceName() {
        return marketAppServiceName;
    }

    public void setMarketAppServiceName(String marketAppServiceName) {
        this.marketAppServiceName = marketAppServiceName;
    }

    public String getMarketAppServiceCode() {
        return marketAppServiceCode;
    }

    public void setMarketAppServiceCode(String marketAppServiceCode) {
        this.marketAppServiceCode = marketAppServiceCode;
    }

    public String getSourceCodeUrl() {
        return sourceCodeUrl;
    }

    public void setSourceCodeUrl(String sourceCodeUrl) {
        this.sourceCodeUrl = sourceCodeUrl;
    }
}
