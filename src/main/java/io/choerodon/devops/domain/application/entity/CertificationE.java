package io.choerodon.devops.domain.application.entity;

import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by n!Ck
 * Date: 2018/8/21
 * Time: 10:30
 * Description:
 */
public class CertificationE {
    private Long id;
    private String name;
    private DevopsEnvironmentE environmentE;
    private List<String> domains;
    private String status;

    public CertificationE() {
    }

    public CertificationE(Long id, String name, DevopsEnvironmentE environmentE, List<String> domains, String status) {
        this.id = id;
        this.name = name;
        this.environmentE = environmentE;
        this.domains = domains;
        this.status = status;
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

    public DevopsEnvironmentE getEnvironmentE() {
        return environmentE;
    }

    public void setEnvironmentE(DevopsEnvironmentE environmentE) {
        this.environmentE = environmentE;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
