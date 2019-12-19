package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

public class MarketDelGitlabProPayload {
    private List<String> listAppServiceCode;
    private String appCode;
    private Long mktAppId;
    private Long gitlabUserId;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }

    public List<String> getListAppServiceCode() {
        return listAppServiceCode;
    }

    public void setListAppServiceCode(List<String> listAppServiceCode) {
        this.listAppServiceCode = listAppServiceCode;
    }

    public Long getMktAppId() {
        return mktAppId;
    }

    public void setMktAppId(Long mktAppId) {
        this.mktAppId = mktAppId;
    }
}
