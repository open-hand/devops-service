package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator: Runge
 * Date: 2018/6/20
 * Time: 10:10
 * Description:
 */
public class AppServiceMarketTgzVO {
    private List<AppServiceReleasingVO> appMarketList;
    private String fileCode;

    public AppServiceMarketTgzVO() {
        this.appMarketList = new ArrayList<>();
    }

    public List<AppServiceReleasingVO> getAppMarketList() {
        return appMarketList;
    }

    public void setAppMarketList(List<AppServiceReleasingVO> appMarketList) {
        this.appMarketList = appMarketList;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }
}
