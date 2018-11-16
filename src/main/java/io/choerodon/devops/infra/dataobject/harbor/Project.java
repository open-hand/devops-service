package io.choerodon.devops.infra.dataobject.harbor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Project {
    @JsonProperty("project_name")
    private String name;
    @JsonProperty("public")
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
