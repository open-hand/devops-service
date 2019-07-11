package io.choerodon.devops.infra.dto;

import javax.persistence.Id;
import javax.persistence.Table;


@Table(name = "devops_certification_pro_rel")
public class DevopsCertificationProRelDO {

    @Id
    private Long certId;

    private Long projectId;


    public Long getCertId() {
        return certId;
    }

    public void setCertId(Long certId) {
        this.certId = certId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
