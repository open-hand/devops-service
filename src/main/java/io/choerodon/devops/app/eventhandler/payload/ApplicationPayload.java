package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:56 2019/8/5
 * Description:
 */
public class ApplicationPayload {
    private Long iamUserId;
    private Long appId;
    private String filePath;
    private String groupPath;
    private List<AppServicePayload> appServicePayloads;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<AppServicePayload> getAppServicePayloads() {
        return appServicePayloads;
    }

    public void setAppServicePayloads(List<AppServicePayload> appServicePayloads) {
        this.appServicePayloads = appServicePayloads;
    }

    public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }
}
