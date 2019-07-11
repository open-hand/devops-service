package io.choerodon.devops.api.vo.gitlab;

import javax.validation.constraints.NotNull;

/**
 * Created by n!Ck
 * Date: 2018/10/29
 * Time: 17:49
 * Description:
 */
public class MemberVO {
    @NotNull
    private Integer userId;
    @NotNull
    private Integer accessLevel;
    private String expiresAt;

    public MemberVO(@NotNull Integer userId, @NotNull Integer accessLevel, String expiresAt) {
        this.userId = userId;
        this.accessLevel = accessLevel;
        this.expiresAt = expiresAt;
    }

    public MemberVO(@NotNull Integer userId, @NotNull Integer accessLevel) {
        this.userId = userId;
        this.accessLevel = accessLevel;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}
