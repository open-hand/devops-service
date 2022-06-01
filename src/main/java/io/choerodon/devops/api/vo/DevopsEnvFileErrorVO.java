package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Creator: Runge
 * Date: 2018/8/9
 * Time: 20:57
 * Description:
 */
public class DevopsEnvFileErrorVO {
    @Encrypt
    private Long id;
    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;
    @ApiModelProperty("文件路径")
    private String filePath;
    @ApiModelProperty("gitlab 地址")
    private String fileUrl;
    @ApiModelProperty("commit sha")
    private String commit;
    @ApiModelProperty("错误信息")
    private String error;
    @ApiModelProperty("commit url")
    private String commitUrl;
    @ApiModelProperty("更新时间")
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
