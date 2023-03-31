package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class C7nCertificationCreateOrUpdateVO {
    private String id;
    private String certName;
    private String commonName;
    private List<String> domains;
    private String type;
    private String certId;
    private String keyValue;
    private String certValue;
    private String status;
    private String envId;
    private String envName;
    private Boolean envConnected;
    private Long objectVersionNumber;
    @ApiModelProperty("操作类型 create/update")
    private String operateType;

    private List<CertificationNotifyObject> certificationNotifyObjects;

    private String notifyObjectsJsonStr;

    @ApiModelProperty("是否设置到期前通知")
    private Boolean expireNotice;

    @ApiModelProperty("到期提前多长时间通知")
    private Integer advanceDays;

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public List<CertificationNotifyObject> getNotifyObjects() {
        return certificationNotifyObjects;
    }

    public void setNotifyObjects(List<CertificationNotifyObject> certificationNotifyObjects) {
        this.certificationNotifyObjects = certificationNotifyObjects;
    }

    public String getNotifyObjectsJsonStr() {
        return notifyObjectsJsonStr;
    }

    public void setNotifyObjectsJsonStr(String notifyObjectsJsonStr) {
        this.notifyObjectsJsonStr = notifyObjectsJsonStr;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
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
}
