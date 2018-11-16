package io.choerodon.devops.api.dto;

public class DevopsMergeRequestDTO {

    private String objectKind;

    private ProjectDTO project;

    private ObjectAttributesDTO objectAttributes;

    public ProjectDTO getProject() {
        return project;
    }

    public void setProject(ProjectDTO project) {
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
