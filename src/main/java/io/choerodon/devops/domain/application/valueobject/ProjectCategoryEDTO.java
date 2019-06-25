package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:56 2019/6/24
 * Description:
 */
public class ProjectCategoryEDTO {
    private Long id;
    private String name;
    private String description;
    private String code;
    private Long organizationId;
    private Boolean displayFlag;
    private Boolean builtInFlag;
    private Long objectVersionNumber;
    private List<MenuCodeDTO> menuCodes;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Boolean getDisplayFlag() {
        return displayFlag;
    }

    public void setDisplayFlag(Boolean displayFlag) {
        this.displayFlag = displayFlag;
    }

    public Boolean getBuiltInFlag() {
        return builtInFlag;
    }

    public void setBuiltInFlag(Boolean builtInFlag) {
        this.builtInFlag = builtInFlag;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public List<MenuCodeDTO> getMenuCodes() {
        return menuCodes;
    }

    public void setMenuCodes(List<MenuCodeDTO> menuCodes) {
        this.menuCodes = menuCodes;
    }
}
