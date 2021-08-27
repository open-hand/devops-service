package io.choerodon.devops.api.vo.harbor;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/30 15:27
 */
public class ProdImageInfoVO {
    @ApiModelProperty("仓库名 比如example.harbor.com")
    private String repoName;

    @ApiModelProperty("仓库类型")
    private String repoType;

    @Encrypt
    @ApiModelProperty("仓库Id")
    private String repoId;

    @ApiModelProperty("镜像名称 可能是example.harbor.com/project-1/mysql 也可能是 mysql")
    private String imageName;

    @ApiModelProperty("镜像版本")
    private String tag;

    private String customImageName;

    private String username;

    private String password;

    private Boolean privateRepository;

    public Boolean getPrivateRepository() {
        return privateRepository;
    }

    public void setPrivateRepository(Boolean privateRepository) {
        this.privateRepository = privateRepository;
    }

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


    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCustomImageName() {
        return customImageName;
    }

    public void setCustomImageName(String customImageName) {
        this.customImageName = customImageName;
    }
}
