package io.choerodon.devops.infra.dto.harbor;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sheep on 2019/4/29.
 */
public class SystemInfo {

    @SerializedName("harbor_version")
    private String harborVersion;


    public String getHarborVersion() {
        return harborVersion;
    }

    public void setHarborVersion(String harborVersion) {
        this.harborVersion = harborVersion;
    }
}
