package io.choerodon.devops.api.vo;

import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;

/**
 * Created by Zenger on 2018/3/28.
 */
@Component
@Scope("prototype")
public class ProjectVO {
    private Long id;
    private String name;
    private Long organizationId;
    private String code;
    private DevopsProjectVO devopsProjectE;

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

    public DevopsProjectVO getDevopsProjectE() {
        return devopsProjectE;
    }

    public void setDevopsProjectE(DevopsProjectVO devopsProjectE) {
        this.devopsProjectE = devopsProjectE;
    }

}
