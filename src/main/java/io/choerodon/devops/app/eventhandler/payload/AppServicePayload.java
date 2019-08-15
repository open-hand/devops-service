package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

import io.choerodon.devops.api.vo.ConfigVO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  13:48 2019/8/2
 * Description:
 */
public class AppServicePayload {
    private Long appId;
    private String name;
    private String code;
    private String type;
    private ConfigVO harbor;
    private List<AppServiceVersionPayload> appServiceVersionPayloads;

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

    public ConfigVO getHarbor() {
        return harbor;
    }

    public void setHarbor(ConfigVO harbor) {
        this.harbor = harbor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<AppServiceVersionPayload> getAppServiceVersionPayloads() {
        return appServiceVersionPayloads;
    }

    public void setAppServiceVersionPayloads(List<AppServiceVersionPayload> appServiceVersionPayloads) {
        this.appServiceVersionPayloads = appServiceVersionPayloads;
    }
}
