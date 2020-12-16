package io.choerodon.devops.api.vo.market;

/**
 * Created by wangxiang on 2020/12/15
 */
public class RepoConfigVO {

    private String marketAppName;

    private String marketServiceName;



    private MarketMavenConfigVO marketMavenConfigVO;

    private MarketHarborConfigVO marketHarborConfigVO;

    private MarketChartConfigVO marketChartConfigVO;

    public String getMarketAppName() {
        return marketAppName;
    }

    public void setMarketAppName(String marketAppName) {
        this.marketAppName = marketAppName;
    }

    public String getMarketServiceName() {
        return marketServiceName;
    }

    public void setMarketServiceName(String marketServiceName) {
        this.marketServiceName = marketServiceName;
    }

    public MarketMavenConfigVO getMarketMavenConfigVO() {
        return marketMavenConfigVO;
    }

    public void setMarketMavenConfigVO(MarketMavenConfigVO marketMavenConfigVO) {
        this.marketMavenConfigVO = marketMavenConfigVO;
    }

    public MarketHarborConfigVO getMarketHarborConfigVO() {
        return marketHarborConfigVO;
    }

    public void setMarketHarborConfigVO(MarketHarborConfigVO marketHarborConfigVO) {
        this.marketHarborConfigVO = marketHarborConfigVO;
    }

    public MarketChartConfigVO getMarketChartConfigVO() {
        return marketChartConfigVO;
    }

    public void setMarketChartConfigVO(MarketChartConfigVO marketChartConfigVO) {
        this.marketChartConfigVO = marketChartConfigVO;
    }
}
