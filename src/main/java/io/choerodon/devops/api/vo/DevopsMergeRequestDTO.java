package io.choerodon.devops.api.vo;

public class DevopsMergeRequestDTO {

    private String objectKind;

    private ProjectReqVO project;

    private ObjectAttributesDTO objectAttributes;

    public ProjectReqVO getProject() {
        return project;
    }

    public void setProject(ProjectReqVO project) {
        this.project = project;
    }

    public ObjectAttributesDTO getObjectAttributes() {
        return objectAttributes;
    }

    public void setObjectAttributes(ObjectAttributesDTO objectAttributesDTODTO) {
        this.objectAttributes = objectAttributesDTODTO;
    }

    public String getObjectKind() {
        return objectKind;
    }

    public void setObjectKind(String objectKind) {
        this.objectKind = objectKind;
    }
}
