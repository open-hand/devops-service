package io.choerodon.devops.infra.dto.harbor;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sheep on 2019/4/29.
 */
public class MemberUser {

    @SerializedName("user_id")
    private Integer userId;
    private String username;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
