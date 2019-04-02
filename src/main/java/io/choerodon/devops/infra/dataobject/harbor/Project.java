package io.choerodon.devops.infra.dataobject.harbor;

import com.google.gson.annotations.SerializedName;

public class Project {
    @SerializedName("project_name")
    private String name;
    @SerializedName("public")
    private Integer isPublic;

    public Project(String name, Integer isPublic) {
        this.name = name;
        this.isPublic = isPublic;
    }

    public String getName() {
        return name;
    }

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public void setName(String name) {
        this.name = name;
    }
}
