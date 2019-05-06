package io.choerodon.devops.infra.dataobject.harbor;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sheep on 2019/4/28.
 */
public class Project {

    @SerializedName("project_name")
    private String name;
    @SerializedName("public")
    private Integer isPublic;

    private Metadata metadata;

    public Project(String name, Integer isPublic) {
        this.name = name;
        this.isPublic = isPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
