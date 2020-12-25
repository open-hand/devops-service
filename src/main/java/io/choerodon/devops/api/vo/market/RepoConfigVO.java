package io.choerodon.devops.api.vo.market;

/**
 * Created by wangxiang on 2020/12/15
 */
public class RepoConfigVO {

    private MarketServiceVO marketServiceVO;

    private MarketServiceDeployObjectVO marketServiceDeployObjectDTO;

    private MarketMavenConfigVO marketMavenConfigVO;

    private MarketHarborConfigVO marketHarborConfigVO;

    private MarketChartConfigVO marketChartConfigVO;


    public MarketServiceVO getMarketServiceVO() {
        return marketServiceVO;
    }

    public void setMarketServiceVO(MarketServiceVO marketServiceVO) {
        this.marketServiceVO = marketServiceVO;
    }

    public MarketServiceDeployObjectVO getMarketServiceDeployObjectDTO() {
        return marketServiceDeployObjectDTO;
    }

    public void setMarketServiceDeployObjectDTO(MarketServiceDeployObjectVO marketServiceDeployObjectDTO) {
        this.marketServiceDeployObjectDTO = marketServiceDeployObjectDTO;
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
