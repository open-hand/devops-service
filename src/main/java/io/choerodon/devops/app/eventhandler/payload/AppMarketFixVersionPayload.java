package io.choerodon.devops.app.eventhandler.payload;

import io.choerodon.devops.api.vo.iam.MarketApplicationVO;

import io.swagger.annotations.ApiModelProperty;

/**
 * Creator: longhe6699@gmail.com
 * Date:  18:59 2019/8/27
 * Description:
 */
public class AppMarketFixVersionPayload {
    @ApiModelProperty("市场应用更新信息")
    private MarketApplicationVO marketApplicationVO;

    @ApiModelProperty("修复版本信息")
    private AppMarketUploadPayload fixVersionUploadPayload;

    public MarketApplicationVO getMarketApplicationVO() {
        return marketApplicationVO;
    }

    public void setMarketApplicationVO(MarketApplicationVO marketApplicationVO) {
        this.marketApplicationVO = marketApplicationVO;
    }

    public AppMarketUploadPayload getFixVersionUploadPayload() {
        return fixVersionUploadPayload;
    }

    public void setFixVersionUploadPayload(AppMarketUploadPayload fixVersionUploadPayload) {
        this.fixVersionUploadPayload = fixVersionUploadPayload;
    }
}
