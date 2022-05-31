package io.choerodon.devops.api.vo.rdupm;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 10:33
 */
public class ProdJarInfoVO {
    @Encrypt
    @ApiModelProperty("制品库nexus服务id")
    private Long nexusId;

    @Encrypt
    @ApiModelProperty("仓库id")
    private Long repositoryId;

    @ApiModelProperty("groupId")
    private String groupId;

    @ApiModelProperty("artifactId")
    private String artifactId;

    @ApiModelProperty("版本")
    private String version;

    /**
     * 流水线jar包上传到自定义仓库时使用
     */
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("下载地址")
    private String downloadUrl;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Long getNexusId() {
        return nexusId;
    }

    public void setNexusId(Long nexusId) {
        this.nexusId = nexusId;
    }

    public ProdJarInfoVO() {
    }

    public ProdJarInfoVO(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public ProdJarInfoVO(Long repositoryId, String groupId, String artifactId, String version) {
        this.repositoryId = repositoryId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
    public ProdJarInfoVO(Long nexusId, Long repositoryId, String groupId, String artifactId, String version) {
        this.nexusId = nexusId;
        this.repositoryId = repositoryId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
