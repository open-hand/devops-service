package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:27
 * Description:
 */
public class CertificationVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("组织id")
    private Long organizationId;
    @ApiModelProperty("证书名称")
    private String certName;
    @ApiModelProperty("泛域名")
    private String commonName;
    @ApiModelProperty("绑定的域名,如果证书类型是选择证书，将会被设置成去掉泛域名后的前缀部分")
    private List<String> domains;
    @ApiModelProperty("绑定的完整域名")
    private List<String> fullDomains;
    @ApiModelProperty("证书类型")
    private String type;
    @ApiModelProperty("证书状态")
    private String status;
    @ApiModelProperty("生效时间")
    private Date validFrom;
    @ApiModelProperty("过期时间")
    private Date validUntil;
    @Encrypt
    @ApiModelProperty("环境id")
    private Long envId;
    @ApiModelProperty("环境名称")
    private String envName;
    @ApiModelProperty("环境是否连接")
    private Boolean envConnected;
    @ApiModelProperty("命令类型")
    private String commandType;
    @ApiModelProperty("命令状态")
    private String commandStatus;
    @ApiModelProperty("命令错误")
    private String error;
    @ApiModelProperty("是否跳过权限校验")
    private Boolean skipCheckProjectPermission;
    private String certValue;
    private String keyValue;
    @Encrypt
    private Long certId;

    private List<CertificationNotifyObject> notifyObjects;

    private String notifyObjectsJsonStr;

    @ApiModelProperty("是否设置到期前通知")
    private Boolean expireNotice;

    @ApiModelProperty("到期提前多长时间通知")
    private Integer advanceDays;

    private Long objectVersionNumber;

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
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

    public List<CertificationNotifyObject> getNotifyObjects() {
        return notifyObjects;
    }

    public void setNotifyObjects(List<CertificationNotifyObject> notifyObjects) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Boolean getSkipCheckProjectPermission() {
        return skipCheckProjectPermission;
    }

    public void setSkipCheckProjectPermission(Boolean skipCheckProjectPermission) {
        this.skipCheckProjectPermission = skipCheckProjectPermission;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public List<String> getFullDomains() {
        return fullDomains;
    }

    public void setFullDomains(List<String> fullDomains) {
        this.fullDomains = fullDomains;
    }
}
