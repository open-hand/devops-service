package io.choerodon.devops.domain.application.entity;

/**
 * Created by Sheep on 2019/3/14.
 */
public class DevopsRegistrySecretE {

    private Long id;
    private DevopsEnvironmentE devopsEnvironmentE;
    private DevopsProjectConfigE devopsProjectConfigE;
    private String secretCode;
    private String secretDetail;
    private Boolean status;


    public DevopsRegistrySecretE() {
    }

    public DevopsRegistrySecretE(Long envId, Long configId, String secretCode, String secretDetail) {
        this.devopsEnvironmentE = new DevopsEnvironmentE(envId);
        this.devopsProjectConfigE = new DevopsProjectConfigE(configId);
        this.secretCode = secretCode;
        this.secretDetail = secretDetail;
        this.status = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DevopsEnvironmentE getDevopsEnvironmentE() {
        return devopsEnvironmentE;
    }

    public void initDevopsEnvironmentE(Long id) {
        this.devopsEnvironmentE = new DevopsEnvironmentE(id);
    }

    public DevopsProjectConfigE getDevopsProjectConfigE() {
        return devopsProjectConfigE;
    }

    public void initDevopsProjectConfigE(Long configId) {
        this.devopsProjectConfigE = new DevopsProjectConfigE(configId);
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public String getSecretDetail() {
        return secretDetail;
    }

    public void setSecretDetail(String secretDetail) {
        this.secretDetail = secretDetail;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
