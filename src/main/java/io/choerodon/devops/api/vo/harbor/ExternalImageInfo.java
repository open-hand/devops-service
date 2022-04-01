package io.choerodon.devops.api.vo.harbor;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by wangxiang on 2022/2/18
 */
public class ExternalImageInfo {

    @ApiModelProperty("镜像地址")
    private String imageUrl;


    private String username;

    private String password;

    @ApiModelProperty("仓库是不是私库有")
    private Boolean privateRepository;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public Boolean getPrivateRepository() {
        return privateRepository;
    }

    public void setPrivateRepository(Boolean privateRepository) {
        this.privateRepository = privateRepository;
    }
}
