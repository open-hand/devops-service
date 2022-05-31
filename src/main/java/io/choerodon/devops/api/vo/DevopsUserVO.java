package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 11:32
 * Description:
 */
public class DevopsUserVO {
    @Encrypt
    @ApiModelProperty("iamUserId")
    private Long iamUserId;
    @ApiModelProperty("登录名")
    private String loginName;
    @ApiModelProperty("真实姓名")
    private String realName;
    @ApiModelProperty("头像地址")
    private String imageUrl;

    public DevopsUserVO() {
    }

    public DevopsUserVO(Long iamUserId, String loginName, String realName) {
        this.iamUserId = iamUserId;
        this.loginName = loginName;
        this.realName = realName;
    }

    public DevopsUserVO(Long iamUserId, String loginName, String realName, String imageUrl) {
        this.iamUserId = iamUserId;
        this.loginName = loginName;
        this.realName = realName;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
