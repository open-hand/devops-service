package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * GitLab WebHook 中Job类型数据的commit字段数据结构
 *
 * @author zmf
 * @since 19-12-24
 */
public class JobCommitVO {
    @ApiModelProperty("其实是pipeline的id")
    private Long id;
    private String sha;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "JobCommitVO{" +
                "id=" + id +
                ", sha='" + sha + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
