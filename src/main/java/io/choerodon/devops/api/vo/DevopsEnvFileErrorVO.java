package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.Date;

/**
 * Creator: Runge
 * Date: 2018/8/9
 * Time: 20:57
 * Description:
 */
public class DevopsEnvFileErrorVO {

    @Encrypt(DevopsEnvFileErrorDTO.ENCRYPT_KEY)
    private Long id;
    private Long envId;
    private String filePath;
    private String fileUrl;
    private String commit;
    private String error;
    private String commitUrl;
    private Date lastUpdateDate;

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

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
