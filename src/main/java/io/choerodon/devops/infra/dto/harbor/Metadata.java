package io.choerodon.devops.infra.dto.harbor;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sheep on 2019/4/28.
 */
public class Metadata {

    @SerializedName("public")
    private String harborPublic;

    public String getHarborPublic() {
        return harborPublic;
    }

    public void setHarborPublic(String harborPublic) {
        this.harborPublic = harborPublic;
    }
}
