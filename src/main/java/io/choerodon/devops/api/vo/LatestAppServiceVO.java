package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author lihao
 */
public class LatestAppServiceVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("应用服务名称")
    private String name;
    @ApiModelProperty("应用服务编码")
    private String code;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("项目名称")
    private String projectName;
    @ApiModelProperty("gitlab仓库地址")
    private String repoUrl;
    @ApiModelProperty("最近更新时间")
    private Date lastUpdateDate;

    public Long getId() {
        return id;
    }

    public LatestAppServiceVO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public LatestAppServiceVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getCode() {
        return code;
    }

    public LatestAppServiceVO setCode(String code) {
        this.code = code;
        return this;
    }

    public Long getProjectId() {
        return projectId;
    }

    public LatestAppServiceVO setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public LatestAppServiceVO setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public LatestAppServiceVO setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
        return this;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public LatestAppServiceVO setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
        return this;
    }

    @Override
    public String toString() {
        return "LatestAppServiceVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", gitlabUrl='" + repoUrl + '\'' +
                ", lastUpdateDate=" + lastUpdateDate +
                '}';
    }
}
