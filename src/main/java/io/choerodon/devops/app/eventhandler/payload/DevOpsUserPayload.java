package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

/**
 * Created by n!Ck
 * Date: 2018/11/28
 * Time: 13:58
 * Description:
 */
public class DevOpsUserPayload {
    private Long iamProjectId;
    private Long appServiceId;
    private Integer gitlabProjectId;
    private List<Long> iamUserIds;
    private Integer option;

    public Long getIamProjectId() {
        return iamProjectId;
    }

    public void setIamProjectId(Long iamProjectId) {
        this.iamProjectId = iamProjectId;
    }

    public Long getAppId() {
        return appServiceId;
    }

    public void setAppId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Integer getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Integer gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public List<Long> getIamUserIds() {
        return iamUserIds;
    }

    public void setIamUserIds(List<Long> iamUserIds) {
        this.iamUserIds = iamUserIds;
    }

    public Integer getOption() {
        return option;
    }

    public void setOption(Integer option) {
        this.option = option;
    }
}

