package io.choerodon.devops.api.vo.notify;


import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 消息业务类型
 */
public class SendSettingDTO extends AuditDomain {

    private Long id;

    private String code;

    private String name;

    private String description;

    private String level;

    private String categoryCode;

    private Boolean isAllowConfig;

    private Boolean enabled;

    private Integer retryCount;

    private Boolean isSendInstantly;

    private Boolean isManualRetry;

    private Boolean emailEnabledFlag;

    private Boolean pmEnabledFlag;

    private Boolean smsEnabledFlag;

    private Boolean webhookEnabledFlag;

    private Boolean backlogFlag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public SendSettingDTO setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getLevel() {
        return level;
    }

    public SendSettingDTO setLevel(String level) {
        this.level = level;
        return this;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Boolean getIsSendInstantly() {
        return isSendInstantly;
    }

    public void setIsSendInstantly(Boolean sendInstantly) {
        isSendInstantly = sendInstantly;
    }

    public Boolean getIsManualRetry() {
        return isManualRetry;
    }

    public void setIsManualRetry(Boolean manualRetry) {
        isManualRetry = manualRetry;
    }

    public Boolean getAllowConfig() {
        return isAllowConfig;
    }

    public void setAllowConfig(Boolean allowConfig) {
        isAllowConfig = allowConfig;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public SendSettingDTO setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public Boolean getEmailEnabledFlag() {
        return emailEnabledFlag;
    }

    public void setEmailEnabledFlag(Boolean emailEnabledFlag) {
        this.emailEnabledFlag = emailEnabledFlag;
    }

    public Boolean getPmEnabledFlag() {
        return pmEnabledFlag;
    }

    public void setPmEnabledFlag(Boolean pmEnabledFlag) {
        this.pmEnabledFlag = pmEnabledFlag;
    }

    public Boolean getSmsEnabledFlag() {
        return smsEnabledFlag;
    }

    public void setSmsEnabledFlag(Boolean smsEnabledFlag) {
        this.smsEnabledFlag = smsEnabledFlag;
    }

    public Boolean getWebhookEnabledFlag() {
        return webhookEnabledFlag;
    }

    public SendSettingDTO setWebhookEnabledFlag(Boolean webhookEnabledFlag) {
        this.webhookEnabledFlag = webhookEnabledFlag;
        return this;
    }

    public Boolean getBacklogFlag() {
        return backlogFlag;
    }

    public void setBacklogFlag(Boolean backlogFlag) {
        this.backlogFlag = backlogFlag;
    }
}
