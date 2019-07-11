package io.choerodon.devops.infra.dto.harbor;

import com.google.gson.annotations.SerializedName;

public class ProjectDetail {

    private String name;
    @SerializedName("project_id")
    private Integer projectId;
    private Metadata metadata;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
