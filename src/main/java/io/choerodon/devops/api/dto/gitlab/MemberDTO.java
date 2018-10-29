package io.choerodon.devops.api.dto.gitlab;

import javax.validation.constraints.NotNull;

/**
 * Created by n!Ck
 * Date: 2018/10/29
 * Time: 17:49
 * Description:
 */
public class MemberDTO {
    @NotNull
    private Integer userId;
    @NotNull
    private Integer accessLevel;
    private String expiresAt;

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
