package io.choerodon.devops.api.vo;

import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 11:32
 * Description:
 */
public class DevopsUserVO {
    @Encrypt
    private Long iamUserId;
    private String loginName;
    private String realName;
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
