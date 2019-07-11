package io.choerodon.devops.api.vo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.iam.entity.DevopsProjectE;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;

/**
 * Created by Zenger on 2018/3/28.
 */
@Component
@Scope("prototype")
public class ProjectVO {
    private Long id;
    private OrganizationVO organization;
    private String name;
    private String code;
    private DevopsProjectE devopsProjectE;

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

    public OrganizationVO getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationVO organization) {
        this.organization = organization;
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

    public DevopsProjectE getDevopsProjectE() {
        return devopsProjectE;
    }

    public void setDevopsProjectE(DevopsProjectE devopsProjectE) {
        this.devopsProjectE = devopsProjectE;
    }

    public void initOrganization(Long id) {
        this.organization = new OrganizationVO(id);
    }
}
