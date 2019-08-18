package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

import io.choerodon.devops.api.vo.ConfigVO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  13:48 2019/8/2
 * Description:
 */
public class AppServiceDownloadVO {
    private Long appId;
    private String name;
    private String code;
    private String type;
    private ConfigVO configVO;
    private List<AppServiceVersionDownloadVO> appServiceVersionDownloadVOS;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ConfigVO getConfigVO() {
        return configVO;
    }

    public void setConfigVO(ConfigVO configVO) {
        this.configVO = configVO;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<AppServiceVersionDownloadVO> getAppServiceVersionDownloadVOS() {
        return appServiceVersionDownloadVOS;
    }

    public void setAppServiceVersionDownloadVOS(List<AppServiceVersionDownloadVO> appServiceVersionDownloadVOS) {
        this.appServiceVersionDownloadVOS = appServiceVersionDownloadVOS;
    }
}
