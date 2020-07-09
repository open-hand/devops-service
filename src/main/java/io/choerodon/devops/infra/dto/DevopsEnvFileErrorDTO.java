package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.*;

@ModifyAudit
@VersionAudit
@Table(name = "devops_env_file_error")
public class DevopsEnvFileErrorDTO extends AuditDomain {

    public static final String ENCRYPT_KEY = "devops_env_file_error";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Encrypt(DevopsEnvFileErrorDTO.ENCRYPT_KEY)
    private Long id;
    private Long envId;
    private String filePath;
    private String commit;
    private String error;


    public DevopsEnvFileErrorDTO() {
    }


    public DevopsEnvFileErrorDTO(Long envId, String filePath) {
        this.envId = envId;
        this.filePath = filePath;
    }

    @Transient
    private String resource;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
