package io.choerodon.devops.api.vo.iam;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:30 2019/6/28
 * Description:
 */
public class MarketImageUrlVO {
    private String appCode;
    private List<MarketAppServiceImageVO> serviceImageVOS;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public List<MarketAppServiceImageVO> getServiceImageVOS() {
        return serviceImageVOS;
    }

    public void setServiceImageVOS(List<MarketAppServiceImageVO> serviceImageVOS) {
        this.serviceImageVOS = serviceImageVOS;
    }
}
