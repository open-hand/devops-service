package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Creator: Runge
 * Date: 2018/8/20
 * Time: 17:27
 * Description:
 */
public class C7nCertificationVO {
    @Encrypt
    private Long id;
    private String certName;
    private String commonName;
    private List<String> domains;
    private String type;
    @Encrypt
    private Long certId;
    private String keyValue;
    private String certValue;
    private String status;
    @Encrypt
    private Long envId;
    private String envName;
    private Boolean envConnected;
    private Long objectVersionNumber;

    private List<CertificationNotifyObject> notifyObjects;

    @ApiModelProperty("是否设置到期前通知")
    private Boolean expireNotice;

    @ApiModelProperty("到期提前多长时间通知")
    private Integer advanceDays;

    public List<CertificationNotifyObject> getNotifyObjects() {
        return notifyObjects;
    }

    public void setNotifyObjects(List<CertificationNotifyObject> notifyObjects) {
        this.notifyObjects = notifyObjects;
    }

    public Boolean getExpireNotice() {
        return expireNotice;
    }

    public void setExpireNotice(Boolean expireNotice) {
        this.expireNotice = expireNotice;
    }

    public Integer getAdvanceDays() {
        return advanceDays;
    }

    public void setAdvanceDays(Integer advanceDays) {
        this.advanceDays = advanceDays;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCertName() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = certName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
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

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public Boolean getEnvConnected() {
        return envConnected;
    }

    public void setEnvConnected(Boolean envConnected) {
        this.envConnected = envConnected;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCertId() {
        return certId;
    }

    public void setCertId(Long certId) {
        this.certId = certId;
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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    @Override
    public String toString() {
        return "C7nCertificationVO{" +
                "id=" + id +
                ", certName='" + certName + '\'' +
                ", commonName='" + commonName + '\'' +
                ", domains=" + domains +
                ", type='" + type + '\'' +
                ", certId=" + certId +
                ", keyValue='" + keyValue + '\'' +
                ", certValue='" + certValue + '\'' +
                ", status='" + status + '\'' +
                ", envId=" + envId +
                ", envName='" + envName + '\'' +
                ", envConnected=" + envConnected +
                ", objectVersionNumber=" + objectVersionNumber +
                ", notifyObjects=" + notifyObjects +
                ", expireNotice=" + expireNotice +
                ", advanceDays=" + advanceDays +
                '}';
    }
}
