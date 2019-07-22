package io.choerodon.devops.api.vo;

import java.util.Date;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:52 2019/7/22
 * Description:
 */
public class PipelineUserVO {
    private Long id;
    private String loginName;
    private String realName;
    private String imageUrl;
    private Boolean audit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getAudit() {
        return audit;
    }

    public void setAudit(Boolean audit) {
        this.audit = audit;
    }
}
