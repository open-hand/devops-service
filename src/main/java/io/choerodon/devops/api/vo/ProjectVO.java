package io.choerodon.devops.api.vo;

/**
 * Created by Zenger on 2018/3/28.
 */
public class ProjectVO {
    private Long id;
    private String name;
    private Long organizationId;
    private String code;
    private DevopsProjectVO devopsProjectVO;


    public ProjectVO() {
    }

    public ProjectVO(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public DevopsProjectVO getDevopsProjectVO() {
        return devopsProjectVO;
    }

    public void setDevopsProjectVO(DevopsProjectVO devopsProjectVO) {
        this.devopsProjectVO = devopsProjectVO;
    }


}
