package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class InstanceEventVO {
    @Encrypt
    @ApiModelProperty("这个实例事件所对应的commandId")
    private Long commandId;
    private String type;
    private String status;
    @ApiModelProperty("这次command所对应的错误/可为null")
    private String commandError;
    private Date createTime;
    private String userImage;
    private String loginName;
    private String realName;
    private List<PodEventVO> podEventVO;

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

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

    public List<PodEventVO> getPodEventVO() {
        return podEventVO;
    }

    public void setPodEventVO(List<PodEventVO> podEventVO) {
        this.podEventVO = podEventVO;
    }

    public String getCommandError() {
        return commandError;
    }

    public void setCommandError(String commandError) {
        this.commandError = commandError;
    }
}
