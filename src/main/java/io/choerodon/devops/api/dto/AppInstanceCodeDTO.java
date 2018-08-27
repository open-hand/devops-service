package io.choerodon.devops.api.dto;

/**
 * Created by Zenger on 2018/4/18.
 */
public class AppInstanceCodeDTO {

    private String id;
    private String code;
    private String appVersion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}
