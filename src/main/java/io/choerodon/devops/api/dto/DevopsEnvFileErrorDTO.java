package io.choerodon.devops.api.dto;

import java.util.Date;

/**
 * Creator: Runge
 * Date: 2018/8/9
 * Time: 20:57
 * Description:
 */
public class DevopsEnvFileErrorDTO {
    private Long id;
    private Long envId;
    private String filePath;
    private String commit;
    private String error;
    private String commitUrl;
    private Date errorTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commitSha) {
        this.commit = commitSha;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public Date getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(Date errorTime) {
        this.errorTime = errorTime;
    }
}
