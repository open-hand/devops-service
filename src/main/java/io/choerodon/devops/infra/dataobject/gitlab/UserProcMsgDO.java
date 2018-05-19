package io.choerodon.devops.infra.dataobject.gitlab;

import java.util.Date;
import java.util.List;

public class UserProcMsgDO {
    private String username;    //Gitlab的用户名
    private String email;   //Gitlab的email
    private String name;    //Gitlab的用户昵称
    private Integer projectsLimit;  //Gitlab用户创建项目的限额
    private Date confirmedAt; //Gitlab用户邮箱确认的时间
    private String provider;    //外部登录提供者
    private String externUid;   //Hapcloud用户Id
    private Boolean canCreateGroup; //用户是否可以创建组
    private List<IdentityDO> identities;
    private Boolean skipConfirmation;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getExternUid() {
        return externUid;
    }

    public void setExternUid(String externUid) {
        this.externUid = externUid;
    }

    public Date getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Date confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Integer getProjectsLimit() {
        return projectsLimit;
    }

    public void setProjectsLimit(Integer projectsLimit) {
        this.projectsLimit = projectsLimit;
    }

    public Boolean getCanCreateGroup() {
        return canCreateGroup;
    }

    public void setCanCreateGroup(Boolean canCreateGroup) {
        this.canCreateGroup = canCreateGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<IdentityDO> getIdentities() {
        return identities;
    }

    public void setIdentities(List<IdentityDO> identities) {
        this.identities = identities;
    }

    public Boolean getSkipConfirmation() {
        return skipConfirmation;
    }

    public void setSkipConfirmation(Boolean skipConfirmation) {
        this.skipConfirmation = skipConfirmation;
    }
}
