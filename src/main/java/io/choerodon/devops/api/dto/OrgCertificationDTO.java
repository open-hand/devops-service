package io.choerodon.devops.api.dto;

import java.util.List;

public class OrgCertificationDTO {

    private Long id;
    private String name;
    private String keyValue;
    private String certValue;
    private String domain;
    private List<Long> projects;
    private Boolean skipCheckProjectPermission;


    public OrgCertificationDTO(){


    }

    public OrgCertificationDTO(Long id, String name, String domain, Boolean skipCheckProjectPermission) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getCertValue() {
        return certValue;
    }

    public void setCertValue(String certValue) {
        this.certValue = certValue;
    }

    public List<Long> getProjects() {
        return projects;
    }

    public void setProjects(List<Long> projects) {
        this.projects = projects;
    }

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
