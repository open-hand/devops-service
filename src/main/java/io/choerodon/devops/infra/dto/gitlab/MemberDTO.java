package io.choerodon.devops.infra.dto.gitlab;

import javax.validation.constraints.NotNull;

/**
 * Created by Sheep on 2019/7/12.
 */
public class MemberDTO {

    @NotNull
    private Integer userId;
    @NotNull
    private Integer accessLevel;
    private String expiresAt;

    public MemberDTO(@NotNull Integer userId, @NotNull Integer accessLevel, String expiresAt) {
        this.userId = userId;
        this.accessLevel = accessLevel;
        this.expiresAt = expiresAt;
    }

    public MemberDTO(@NotNull Integer userId, @NotNull Integer accessLevel) {
        this.userId = userId;
        this.accessLevel = accessLevel;
    }

    public MemberDTO() {
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
