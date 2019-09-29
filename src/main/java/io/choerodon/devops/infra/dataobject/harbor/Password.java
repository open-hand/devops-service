package io.choerodon.devops.infra.dataobject.harbor;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sheep on 2019/9/29.
 */
public class Password {

    @SerializedName("new_password")
    private String newPassword;
    @SerializedName("old_password")
    private String oldPassword;

    public Password(){}

    public Password(String newPassword, String oldPassword) {
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
