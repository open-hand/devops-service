package io.choerodon.devops.api.vo.iam;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:30 2019/6/28
 * Description:
 */
public class MarketAppServiceImageVO {
    private String serviceCode;
    private List<MarketAppServiceVersionImageVO> serviceVersionVOS;

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public List<MarketAppServiceVersionImageVO> getServiceVersionVOS() {
        return serviceVersionVOS;
    }

    public void setServiceVersionVOS(List<MarketAppServiceVersionImageVO> serviceVersionVOS) {
        this.serviceVersionVOS = serviceVersionVOS;
    }
}
