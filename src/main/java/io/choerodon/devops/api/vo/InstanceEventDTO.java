package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

public class InstanceEventDTO {
    private String type;
    private String status;
    private Date createTime;
    private String userImage;
    private String loginName;
    private String realName;
    private List<PodEventDTO> podEventDTO;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
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

    public List<PodEventDTO> getPodEventDTO() {
        return podEventDTO;
    }

    public void setPodEventDTO(List<PodEventDTO> podEventDTO) {
        this.podEventDTO = podEventDTO;
    }
}
