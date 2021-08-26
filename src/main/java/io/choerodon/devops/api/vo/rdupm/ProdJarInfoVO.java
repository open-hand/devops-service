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
    @ApiModelProperty("仓库id")
    private Long repositoryId;

    @ApiModelProperty("groupId")
    private String groupId;

    @ApiModelProperty("artifactId")
    private String artifactId;

    @ApiModelProperty("版本")
    private String version;

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
