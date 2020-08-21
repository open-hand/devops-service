package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@ModifyAudit
@VersionAudit
@Table(name = "devops_env_group")
public class DevopsEnvGroupDTO extends AuditDomain {

    public static final String ENCRYPT_KEY = "devops_env_group";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Encrypt(DevopsEnvGroupDTO.ENCRYPT_KEY)
    private Long id;
    private Long projectId;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
