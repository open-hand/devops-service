package io.choerodon.devops.app.eventhandler.payload;

import java.util.List;

/**
 * @author zmf
 */
public class DevopsEnvUserPayload {
    private Long iamProjectId;
    private Long envId;
    private Integer gitlabProjectId;
    private List<Long> iamUserIds;
    private List<Integer> addGitlabUserIds;
    private List<Integer> deleteGitlabUserIds;
    private Integer option;

    public Long getIamProjectId() {
        return iamProjectId;
    }

    public void setIamProjectId(Long iamProjectId) {
        this.iamProjectId = iamProjectId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
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

    public List<Integer> getAddGitlabUserIds() {
        return addGitlabUserIds;
    }

    public void setAddGitlabUserIds(List<Integer> addGitlabUserIds) {
        this.addGitlabUserIds = addGitlabUserIds;
    }

    public List<Integer> getDeleteGitlabUserIds() {
        return deleteGitlabUserIds;
    }

    public void setDeleteGitlabUserIds(List<Integer> deleteGitlabUserIds) {
        this.deleteGitlabUserIds = deleteGitlabUserIds;
    }

    public Integer getOption() {
        return option;
    }

    public void setOption(Integer option) {
        this.option = option;
    }
}
