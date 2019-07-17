package io.choerodon.devops.api.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator: Runge
 * Date: 2018/6/20
 * Time: 10:10
 * Description:
 */
public class AppMarketTgzDTO {
    private List<ApplicationReleasingVO> appMarketList;
    private String fileCode;

    public AppMarketTgzDTO() {
        this.appMarketList = new ArrayList<>();
    }

    public List<ApplicationReleasingVO> getAppMarketList() {
        return appMarketList;
    }

    public void setAppMarketList(List<ApplicationReleasingVO> appMarketList) {
        this.appMarketList = appMarketList;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }
}
