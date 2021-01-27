package io.choerodon.devops.api.vo;

import javax.annotation.Nullable;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-16
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenRepoVO {
    @ApiModelProperty("仓库名称")
    private String name;

    @ApiModelProperty("仓库地址")
    private String url;

    /**
     * 如果是group类型的仓库没有仓库类型
     */
    @Nullable
    @ApiModelProperty("仓库类型 / release, snapshot 多个值逗号分割")
    private String type;

    @ApiModelProperty("是否是私有仓库, true表示是")
    @JsonProperty("private")
    @JSONField(name = "private")
    private Boolean privateRepo;

    @ApiModelProperty("用户名 / 私有仓库必填，公开仓库不填")
    private String username;

    @ApiModelProperty("密码 / 私有仓库必填，公开仓库不填")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JSONField(name = "private")
    @JsonProperty("private")
    public Boolean getPrivateRepo() {
        return privateRepo;
    }

    @JSONField(name = "private")
    @JsonProperty("private")
    public void setPrivateRepo(Boolean privateRepo) {
        this.privateRepo = privateRepo;
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

    @Override
    public String toString() {
        return "MavenRepoVO{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", privateRepo=" + privateRepo +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
