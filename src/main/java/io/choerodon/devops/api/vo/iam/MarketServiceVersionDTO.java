package io.choerodon.devops.api.vo.iam;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:04 2019/8/27
 * Description: 应用市场上传修复版本Payload使用
 */
public class MarketServiceVersionDTO extends BaseDTO {

    private Long id;

    private Long marketServiceId;

    private String version;

    private String imageUrl;

    private String chartUrl;

    private String codeUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMarketServiceId() {
        return marketServiceId;
    }

    public void setMarketServiceId(Long marketServiceId) {
        this.marketServiceId = marketServiceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getChartUrl() {
        return chartUrl;
    }

    public void setChartUrl(String chartUrl) {
        this.chartUrl = chartUrl;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }
}
