package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

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
    @ApiModelProperty("操作类型 create/update")
    private String operateType;

    private List<NotifyObject> notifyObjects;

    private String notifyObjectsJsonStr;

    @ApiModelProperty("是否设置到期前通知")
    private Boolean expireNotice;

    @ApiModelProperty("到期提前多长时间通知")
    private Integer advanceDays;

    /**
     * 通知对象类
     */
    public static class NotifyObject {
        @ApiModelProperty("通知对象类型 user/role")
        private String type;
        @ApiModelProperty("id")
        @Encrypt
        private Long id;
        @Encrypt
        private Long certificationId;

        public NotifyObject() {

        }

        public NotifyObject(String type, Long id, Long certificationId) {
            this.type = type;
            this.id = id;
            this.certificationId = certificationId;
        }

        public Long getCertificationId() {
            return certificationId;
        }

        public void setCertificationId(Long certificationId) {
            this.certificationId = certificationId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public List<NotifyObject> getNotifyObjects() {
        return notifyObjects;
    }

    public void setNotifyObjects(List<NotifyObject> notifyObjects) {
        this.notifyObjects = notifyObjects;
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
