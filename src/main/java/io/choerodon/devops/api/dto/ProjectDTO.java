package io.choerodon.devops.api.dto;

public class ProjectDTO {

    private Long id;
    private String name;
    private String code;
    private Boolean permission;


    public ProjectDTO(Long id, String name, String code, Boolean permission) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.permission = permission;
    }

    public ProjectDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
